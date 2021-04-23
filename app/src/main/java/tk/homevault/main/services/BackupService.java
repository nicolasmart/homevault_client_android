package tk.homevault.main.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.Nullable;

public class BackupService extends Service {
    public Context context = this;
    public Handler handler = null;
    public static Thread backupThread = null;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";
    private static final String PREF_LASTSYNCTIME = "lastsynctime";

    private String serverip;
    private String username;
    private String password;
    private String userrole;
    private String lastsynctime;

    private Runnable keepAlive;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    SharedPreferences pref;
    @Override
    public void onCreate() {
        pref = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);
        lastsynctime = pref.getString(PREF_LASTSYNCTIME, "0");

        handler = new Handler();

        backupThread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                try {
                    lastsynctime = pref.getString(PREF_LASTSYNCTIME, "0");

                    Uri uri;
                    Cursor cursor;
                    int column_index_data, column_index_folder_name;
                    ArrayList<String> listOfAllImages = new ArrayList<String>();
                    String absolutePathOfImage = null;
                    uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                    String[] projection = { MediaStore.MediaColumns.DATA,
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.DATE_ADDED };

                    String[] args = { lastsynctime };
                    cursor = getContentResolver().query(uri, projection, MediaStore.Images.Media.DATE_MODIFIED + " + 0 > (? + 0)",
                            args, MediaStore.Images.Media.DATE_MODIFIED + " ASC");

                    int column_index_d1 = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);
                    int column_index_d2 = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
                    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    column_index_folder_name = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                    Integer lastSyncedInt = Integer.parseInt(lastsynctime);
                    String dateModified;
                    File currImg;
                    while (cursor.moveToNext()) {
                        absolutePathOfImage = cursor.getString(column_index_data);
                        dateModified = cursor.getString(column_index_d2);

                        currImg = new File(absolutePathOfImage);

                        String link=serverip+"/mobile_methods/file_upload.php";

                        Map<String, String> params2 = new HashMap<String, String>(4);
                        params2.put("username", username);
                        params2.put("password", password);
                        params2.put("directory", "/../photos/");
                        params2.put("post_filename", randomAlphaNumericString(16) + ".jpg");

                        String type = null;
                        String extension = FilenameUtils.getExtension(currImg.getName());
                        if (extension != null) {
                            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        }

                        String result = multipartRequest(link, params2, Uri.fromFile(currImg), "file_upload", type);

                        if (Integer.parseInt(dateModified) > lastSyncedInt) {
                            SharedPreferences sharedpreferences = context.getSharedPreferences("core_auth", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString(PREF_LASTSYNCTIME, cursor.getString(column_index_d1));
                            editor.apply();
                            lastsynctime = cursor.getString(column_index_d1);
                            lastSyncedInt = Integer.parseInt(dateModified);
                        }
                    }
                } catch (Exception e) {
                    //Log.d("focused", e.getMessage() + "\n\n" + e.getStackTrace().toString() + "\n\n" + e.getLocalizedMessage() + '\n' + e.getCause());
                }

                handler.postDelayed(keepAlive, 15000);
            }
        };

        keepAlive = new Runnable() {
            public void run() {
                try {
                    backupThread.start();
                } catch (Exception ex) {}
            }
        };

        keepAlive.run();
    }

    @Override
    public void onDestroy() {

    }

    public String randomAlphaNumericString(int n)
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public String multipartRequest(String urlTo, Map<String, String> parmas, Uri fileuri, String filefield, String fileMimeType) throws Exception {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {
            InputStream fileInputStream = getContentResolver().openInputStream(fileuri);

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + getFileDisplayName(fileuri) + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            Iterator<String> keys = parmas.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = parmas.get(key);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            if (200 != connection.getResponseCode()) {
                throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            inputStream = connection.getInputStream();

            result = this.convertStreamToString(inputStream);

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Nullable
    private String getFileDisplayName(final Uri uri) {
        String displayName = null;
        try (Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                //Log.i("Display Name {}" + displayName);

            }
        }

        return displayName;
    }


}

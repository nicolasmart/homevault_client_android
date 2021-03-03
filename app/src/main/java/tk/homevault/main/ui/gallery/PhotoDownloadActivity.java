package tk.homevault.main.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import androidx.core.content.FileProvider;
import tk.homevault.main.R;
import tk.homevault.main.ui.files.FilesFragment;

public class PhotoDownloadActivity extends AsyncTask<String, String, String>{
    private Context context;
    private GalleryFragment galleryFragment;
    private ImageView gridImage;
    private String serverip;
    private String username;
    private String password;
    private String directory;
    private String basefn;
    private File picFile;
    private Uri uri;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public PhotoDownloadActivity(Context context, GalleryFragment galleryFragment, ImageView gridImage) {
        this.context = context;
        this.galleryFragment = galleryFragment;
        this.gridImage = gridImage;
    }

    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... arg0) {
        try{
            serverip = arg0[0];
            username = arg0[1];
            password = arg0[2];
            directory = arg0[3];

            String link="http://"+serverip+"/mobile_methods/file_download.php";
            String data  = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                    URLEncoder.encode(password, "UTF-8");
            data += "&" + URLEncoder.encode("directory", "UTF-8") + "=" +
                    URLEncoder.encode(directory, "UTF-8");

            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setConnectTimeout(100);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setChunkedStreamingMode(1024);

            conn.connect();
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write( data );
            wr.flush();

            String imageName = "IMG_" + String.valueOf(System.currentTimeMillis()) +".jpg";
            picFile = new File(galleryFragment.getActivity().getCacheDir(), imageName);
            uri = FileProvider.getUriForFile(context, "tk.homevault", picFile);

            OutputStream output = context.getContentResolver().openOutputStream(uri);

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = conn.getInputStream().read(buffer)) > 0 ) {
                output.write(buffer, 0, bufferLength);
            }
            output.flush();
            output.close();

            return "done";
        } catch(Exception e){
            return new String(context.getString(R.string.server_not_responding));
        }
    }

    @Override
    protected void onPostExecute(String result){
        gridImage.setAlpha(1f);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryFragment.startActivity(intent);
    }
}

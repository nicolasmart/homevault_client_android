package tk.homevault.main.ui.files;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import tk.homevault.main.R;

public class FileSaveActivity extends AsyncTask<String, String, String>{
    private Context context;
    private FilesFragment filesFragment;
    private Uri uri;
    private InputStream downloadStream;
    private ProgressBar downloadProgress;
    private String serverip;
    private String username;
    private String password;
    private String directory;
    private String basefn;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public FileSaveActivity(Context context, FilesFragment filesFragment, Uri uri, InputStream downloadStream, ProgressBar downloadProgress, String basefn) {
        this.context = context;
        this.filesFragment = filesFragment;
        this.uri = uri;
        this.downloadStream = downloadStream;
        this.downloadProgress = downloadProgress;
        this.basefn = basefn;
    }

    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... arg0) {
        try{
            OutputStream output = context.getContentResolver().openOutputStream(uri);

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = downloadStream.read(buffer)) > 0 ) {
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
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(basefn);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        downloadProgress.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        filesFragment.startActivity(intent);
    }
}

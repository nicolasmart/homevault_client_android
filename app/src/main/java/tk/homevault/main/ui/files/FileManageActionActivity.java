package tk.homevault.main.ui.files;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import tk.homevault.main.R;

public class FileManageActionActivity extends AsyncTask<String, String, String>{
    private Context context;
    private FilesFragment filesFragment;
    private String serverip;
    private String username;
    private String password;
    private String directory;
    private String directory2;
    private String action;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public FileManageActionActivity(Context context, FilesFragment filesFragment) {
        this.context = context;
        this.filesFragment = filesFragment;
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
            directory2 = arg0[4];
            action = arg0[5];

            String link="http://"+serverip+"/mobile_methods/filemanageactions.php";
            String data  = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                    URLEncoder.encode(password, "UTF-8");
            data += "&" + URLEncoder.encode("directory", "UTF-8") + "=" +
                    URLEncoder.encode(directory, "UTF-8");
            data += "&" + URLEncoder.encode("directory2", "UTF-8") + "=" +
                    URLEncoder.encode(directory, "UTF-8");
            data += "&" + URLEncoder.encode("action", "UTF-8") + "=" +
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

            InputStream ird = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(ird));

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null) {
                sb.append(line+'\n');
            }

            return sb.substring(0, sb.length() - 1);
        } catch(Exception e){
            return new String(context.getString(R.string.server_not_responding));
        }
    }

    @Override
    protected void onPostExecute(String result){
        Log.d("FMAA", result);
        filesFragment.refreshPage();
    }
}

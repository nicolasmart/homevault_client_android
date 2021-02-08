package tk.homevault.main.ui.files;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import tk.homevault.main.R;
import tk.homevault.main.login.LoginView;

public class FileFetchActivity extends AsyncTask<String, String, String>{
    private Context context;
    private FilesFragment filesFragment;
    private String serverip;
    private String username;
    private String password;
    private String directory;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public FileFetchActivity(Context context, FilesFragment filesFragment) {
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

            String link="http://"+serverip+"/mobile_methods/filefetch.php";
            String data  = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                    URLEncoder.encode(password, "UTF-8");
            data += "&" + URLEncoder.encode("directory", "UTF-8") + "=" +
                    URLEncoder.encode(directory, "UTF-8");

            long start;

            start = System.currentTimeMillis();
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Cache-Control", "max-age=0");

            Log.d("time1", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            Log.d("time2", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            wr.write( data );
            wr.flush();

            Log.d("time3", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));

            Log.d("time4", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            String line = null;

            Log.d("time5", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            while((line = reader.readLine()) != null) {
                sb.append(line+'\n');
            }

            Log.d("time6", String.valueOf(System.currentTimeMillis()-start));
            start = System.currentTimeMillis();
            return sb.substring(0, sb.length() - 1);
        } catch(Exception e){
            return new String(context.getString(R.string.server_not_responding));
        }
    }

    @Override
    protected void onPostExecute(String result){
        if (!result.contains("listing_success_key")) {
            filesFragment.textView.setText(filesFragment.getString(R.string.error_fetching_data));
            filesFragment.textView.setTypeface(null, Typeface.NORMAL);
            Log.d("FileFetchFail", result);
        }
        else {
            filesFragment.textView.setText(directory);
            filesFragment.textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            filesFragment.textView.setTypeface(null, Typeface.ITALIC);
            filesFragment.rvSetup(result.substring(20));
            Log.d("FileFetchSuccess", result.substring(20));
        }
    }
}

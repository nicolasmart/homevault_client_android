package tk.homevault.main.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import tk.homevault.main.MainActivity;
import tk.homevault.main.R;

public class LoginActionActivity extends AsyncTask<String, String, String>{
    private Context context;
    private Button buttonToDim;
    private String serverip;
    private String username;
    private String password;

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    //flag 0 means get and 1 means post.(By default it is get.)
    public LoginActionActivity(Context context, Button buttonToDim) {
        this.context = context;
        this.buttonToDim = buttonToDim;
    }

    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... arg0) {
        try{
            serverip = arg0[0];
            username = arg0[1];
            password = arg0[2];

            String link="http://"+serverip+"/mobile_methods/auth.php";
            String data  = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                    URLEncoder.encode(password, "UTF-8");

            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write( data );
            wr.flush();

            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));

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
        if (result.contains("login_success_app_handler_key")) {
            SharedPreferences sharedpreferences = context.getSharedPreferences("core_auth", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.clear();
            editor.putString(PREF_SERVERIP, serverip);
            editor.putString(PREF_USERNAME, username);
            editor.putString(PREF_PASSWORD, password);
            editor.putString(PREF_USERROLE, result.substring(result.length()-1));
            editor.apply();

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
        else Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        if (buttonToDim != null) buttonToDim.setAlpha((float) 1);
    }
}

package tk.homevault.main.ui.gallery.backup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.apache.commons.io.FilenameUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

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

import tk.homevault.main.R;
import tk.homevault.main.services.BackupService;
import tk.homevault.main.ui.files.FileUploadActivity;
import tk.homevault.main.ui.gallery.PhotoUploadActivity;

public class BackupActivity extends AppCompatActivity {

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    private String serverip;
    private String username;
    private String password;
    private String userrole;
    private Boolean backupEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences pref = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);
        backupEnabled = pref.getBoolean("backup_enabled", false);

        if (backupEnabled) findViewById(R.id.start_backup_button).setEnabled(false);
    }

    public void startBackup(View v) {
        startService(new Intent(this, BackupService.class));
        SharedPreferences sharedpreferences = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("backup_enabled", true);
        editor.apply();
    }

}

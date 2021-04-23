package tk.homevault.main.ui.gallery.backup;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
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

import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
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

    private TextView storageAvailable, storageTotal;
    private ProgressBar storageBar;
    private SharedPreferences pref;
    private Button backupButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pref = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);
        backupEnabled = pref.getBoolean("backup_enabled", false);

        backupButton = findViewById(R.id.start_backup_button);
        if (backupEnabled) {
            backupButton.setText(getString(R.string.disable_backup));
            backupButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorDarkTransparency));
        }

        storageAvailable = findViewById(R.id.storage_available);
        storageTotal = findViewById(R.id.total_storage);
        storageBar = findViewById(R.id.storage_bar);

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long gigsAvailable = bytesAvailable / (1000 * 1000 * 1000);


        long totalBytes = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long totalGigs = totalBytes / (1000 * 1000 * 1000);

        storageAvailable.setText(String.valueOf(totalGigs-gigsAvailable) + " " + getString(R.string.gb_used));
        storageTotal.setText(getString(R.string.gb_out_of) + " " + String.valueOf(totalGigs) + " " + getString(R.string.gb_used));

        storageBar.setProgress((int)((float)(totalGigs-gigsAvailable)/(float)totalGigs*100));
    }

    public void startBackup(View v) {
        backupEnabled = pref.getBoolean("backup_enabled", false);
        if (backupEnabled) {
            stopBackup();
            return;
        }

        startService(new Intent(this, BackupService.class));
        SharedPreferences sharedpreferences = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("backup_enabled", true);
        editor.apply();

        backupButton.setText(getString(R.string.disable_backup));
        backupButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorDarkTransparency));
    }

    public void stopBackup() {
        stopService(new Intent(this, BackupService.class));
        SharedPreferences sharedpreferences = getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("backup_enabled");
        editor.apply();

        backupButton.setText(getString(R.string.backup_all_photos));
        backupButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

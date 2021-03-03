package tk.homevault.main.login;

import androidx.appcompat.app.AppCompatActivity;
import tk.homevault.main.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class LoginView extends AppCompatActivity {

    private EditText serverIpField, usernameField, passwordField;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        setContentView(R.layout.login_view);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        loginButton = findViewById(R.id.login_button);
        serverIpField = findViewById(R.id.serverip_field);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean mHandled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    loginPost(null);
                    mHandled = true;
                }
                return mHandled;
            }
        });

    }

    public void loginPost(View view){
        String serverip = serverIpField.getText().toString();
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        loginButton.setAlpha((float) 0.3);
        new LoginActionActivity(this,loginButton).execute(serverip, username, password);
    }

    public Bitmap cropCenter(Bitmap bmp) {
        int dimension = Math.min(bmp.getWidth(), bmp.getHeight());
        return ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
    }

}

package tk.homevault.main.ui.notes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import tk.homevault.main.MainActivity;
import tk.homevault.main.R;

public class NotesFragment extends Fragment {

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    private String serverip;
    private String username;
    private String password;
    private String userrole;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notes, container, false);
        //final TextView textView = root.findViewById(R.id.text_send);

        //textView.setText(getString(R.string.coming_soon));
        WebView webView = (WebView) root.findViewById(R.id.webview);

        SharedPreferences pref = getActivity().getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);

        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        webView.getSettings().setJavaScriptEnabled(true);
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<body onload='document.frm1.submit()'>" +
                "<form action='" + serverip + "/notes.php' method='post' name='frm1'>" +
                "  <input type='hidden' name='username' value='" + username + "'><br>" +
                "  <input type='hidden' name='password' value='" + password + "'><br>" +
                (nightModeFlags == Configuration.UI_MODE_NIGHT_YES ? "  <input type='hidden' name='dark_mode' value='1'><br>" : "<br>" ) +
                "</form>" +
                "</body>" +
                "</html>";
        webView.loadData(html, "text/html", "UTF-8");
        Log.d("html", html);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        FloatingActionMenu fab;
        fab = ((MainActivity) getActivity()).fab;
        fab.setVisibility(View.GONE);
        return root;
    }
}
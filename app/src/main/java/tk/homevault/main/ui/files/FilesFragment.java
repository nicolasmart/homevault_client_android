package tk.homevault.main.ui.files;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tk.homevault.main.R;

public class FilesFragment extends Fragment implements FileBrowseRecyclerViewAdapter.ItemClickListener {

    private FileBrowseRecyclerViewAdapter fileViewAdapter;
    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    View root;
    TextView textView;

    private String directory = "/";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_files, container, false);
        textView = root.findViewById(R.id.text_home);
        textView.setText(getString(R.string.fetching_data));

        SharedPreferences pref = getActivity().getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        String serverip = pref.getString(PREF_SERVERIP, null);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        String userrole = pref.getString(PREF_USERROLE, null);

        new FileFetchActivity(getActivity(), this).execute(serverip, username, password, directory);
        return root;
    }

    public void rvSetup(String jsonResult) {
        try {
            ArrayList<String> folderNames = new ArrayList<>();
            ArrayList<String> fileNames = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject curr = jsonArray.getJSONObject(i);
                if (curr.has("dirname")) folderNames.add(curr.getString("dirname"));
                if (curr.has("filename")) fileNames.add(curr.getString("filename"));
            }

            RecyclerView recyclerView = root.findViewById(R.id.rv_files);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            fileViewAdapter = new FileBrowseRecyclerViewAdapter(getActivity(), folderNames, fileNames);
            fileViewAdapter.setClickListener(this);
            recyclerView.setAdapter(fileViewAdapter);
        } catch(Exception ex) {
            textView.setText(getString(R.string.error_fetching_data));
            Log.d("actuallyexcepted", ex.getMessage() + "\n" + ex.getStackTrace().toString() + "\n" + ex.getStackTrace().toString());
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "You clicked " + fileViewAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
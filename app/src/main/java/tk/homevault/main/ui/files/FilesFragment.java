package tk.homevault.main.ui.files;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tk.homevault.main.MainActivity;
import tk.homevault.main.R;
import tk.homevault.main.SettingsActivity;
import tk.homevault.main.ui.gallery.backup.BackupActivity;

public class FilesFragment extends Fragment implements FileBrowseRecyclerViewAdapter.ItemClickListener, FileBrowseRecyclerViewAdapter.ItemLongClickListener {

    private FileBrowseRecyclerViewAdapter fileViewAdapter;
    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    private String serverip;
    private String username;
    private String password;
    private String userrole;

    View root;
    TextView textView;

    private String directory = "/";
    private Boolean backArrow;
    private Integer folderCount;
    private InputStream downloadStream;

    private FloatingActionMenu fab;
    private FloatingActionButton fab1, fab2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_files, container, false);
        textView = root.findViewById(R.id.text_home);
        textView.setText(getString(R.string.fetching_data));

        setHasOptionsMenu(true);

        SharedPreferences pref = getActivity().getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);

        new DirectoryFetchActivity(getActivity(), this).execute(serverip, username, password, directory);

        fab = ((MainActivity) getActivity()).fab;
        fab.setVisibility(View.VISIBLE);
        fab.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.toggle(true);
            }
        });

        fab1 = ((MainActivity) getActivity()).findViewById(R.id.fab1);
        fab2 = ((MainActivity) getActivity()).findViewById(R.id.fab2);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.toggle(true);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 67);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFolder();
                fab.toggle(true);
            }
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void rvSetup(String jsonResult) {
        try {
            backArrow = false;
            ArrayList<String> folderNames = new ArrayList<>();
            ArrayList<String> fileNames = new ArrayList<>();
            if (directory.length()>1) {
                folderNames.add(getString(R.string.go_back));
                backArrow = true;
            }

            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject curr = jsonArray.getJSONObject(i);
                if (curr.has("dirname")) folderNames.add(curr.getString("dirname"));
                if (curr.has("filename")) fileNames.add(curr.getString("filename"));
            }
            folderCount = folderNames.size();

            RecyclerView recyclerView = root.findViewById(R.id.rv_files);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            fileViewAdapter = new FileBrowseRecyclerViewAdapter(getActivity(), folderNames, fileNames, backArrow);
            fileViewAdapter.setClickListener(this);
            fileViewAdapter.setLongClickListener(this);
            recyclerView.setAdapter(fileViewAdapter);
        } catch(Exception ex) {
            textView.setText(getString(R.string.error_fetching_data));
            Log.d("actuallyexcepted", ex.getMessage() + "\n" + ex.getStackTrace().toString() + "\n" + ex.getStackTrace().toString());
        }
    }

    public void setStream(InputStream passFromPhp) {
        downloadStream = passFromPhp;
    }

    private ProgressBar downloadProgress;
    private String baseFilename;

    @Override
    public void onItemClick(View view, int position) {
        if (backArrow && position == 0) {
            directory = directory.substring(0, directory.lastIndexOf('/'));
            if (directory.length() < 1) directory = "/";
        }
        else if (position < folderCount) {
            if (directory.length() > 1) directory = directory + "/" + fileViewAdapter.getItem(position);
            else directory = directory + fileViewAdapter.getItem(position);
        }
        else {
            downloadProgress = view.findViewById(R.id.download_progress);
            downloadProgress.setVisibility(View.VISIBLE);
            baseFilename = fileViewAdapter.getItem(position);

            new FileDownloadActivity(getActivity(), this).execute(serverip, username, password, directory + "/" + baseFilename, baseFilename);
            return;
        }

        new DirectoryFetchActivity(getActivity(), this).execute(serverip, username, password, directory);
    }

    @Override
    public boolean onItemLongClick(View view, final int position) {
        if (backArrow && position == 0) return false;

        final String[] fileOptions = {/**getString(R.string.save_to_device), */ getString(R.string.move_file), getString(R.string.copy_file), getString(R.string.delete_file), getString(R.string.rename)};
        final String[] folderOptions = {getString(R.string.move_file), getString(R.string.copy_file), getString(R.string.delete_file)};
        String[] options = position < folderCount ? folderOptions : fileOptions;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fileViewAdapter.getItem(position));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option_index_def) {
                /**if (position < folderCount)*/ //option_index++;
                final int option_index = option_index_def + 1;
                baseFilename = fileViewAdapter.getItem(position);
                if (option_index>=1 && option_index<=2 || option_index==4) fileDestination(fileOptions[option_index-1], option_index); /// REMOVE THE -1!!!
                else if (option_index == 3) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    new FileManageActionActivity(getActivity(), FilesFragment.this).execute(serverip, username, password, directory + (directory.length()>1 ? "/" : "") + baseFilename, null, String.valueOf(option_index));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.file_deletion_q)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                    //Log.d("FilesFRG", directory + (directory.length()>1 ? "/" : "") + baseFilename);
                    //Log.d("FilesFRG2", String.valueOf(option_index));


                }
            }
        });
        builder.show();
        return true;
    }

    public void refreshPage() {
        new DirectoryFetchActivity(getActivity(), this).execute(serverip, username, password, directory);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 34 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                new FileSaveActivity(getActivity(), this, uri, downloadStream, downloadProgress, baseFilename).execute();
            }
            catch(Exception e) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 34 && resultCode == Activity.RESULT_CANCELED) {
            downloadProgress.setVisibility(View.GONE);
        }
        else if (requestCode == 67 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            new FileUploadActivity(getActivity(), this, uri).execute(serverip, username, password, directory);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings :
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.create_folder));

        TextInputLayout textInputLayout = new TextInputLayout(getActivity());

        final EditText input = new EditText(getActivity());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.folder_name));

        final String blockCharacterSet = "/\\";
        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };

        input.setFilters(new InputFilter[] {filter});

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int left_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, this.getResources().getDisplayMetrics()));
        int top_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, this.getResources().getDisplayMetrics()));
        int right_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, this.getResources().getDisplayMetrics()));
        int bottom_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources().getDisplayMetrics()));
        params.setMargins(left_margin, top_margin, right_margin, bottom_margin);

        textInputLayout.setLayoutParams(params);

        textInputLayout.addView(input);
        container.addView(textInputLayout);

        builder.setView(container);

        builder.setPositiveButton(getString(R.string.create_folder_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()) {
                    return;
                }
                new CreateFolderActivity(getActivity(), FilesFragment.this).execute(serverip, username, password, directory + ((directory.length()>1) ? "/" : "") + input.getText().toString());
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void fileDestination(String title, final int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        TextInputLayout textInputLayout = new TextInputLayout(getActivity());

        final EditText input = new EditText(getActivity());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.file_destination));

        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int left_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, this.getResources().getDisplayMetrics()));
        int top_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, this.getResources().getDisplayMetrics()));
        int right_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, this.getResources().getDisplayMetrics()));
        int bottom_margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources().getDisplayMetrics()));
        params.setMargins(left_margin, top_margin, right_margin, bottom_margin);

        textInputLayout.setLayoutParams(params);

        textInputLayout.addView(input);
        container.addView(textInputLayout);

        builder.setView(container);

        builder.setPositiveButton(title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()) {
                    return;
                }
                new FileManageActionActivity(getActivity(), FilesFragment.this).execute(serverip, username, password, directory + (directory.length()>1 ? "/" : "") +  baseFilename, input.getText().toString(), String.valueOf(action));
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
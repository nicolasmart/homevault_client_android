package tk.homevault.main.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import tk.homevault.main.MainActivity;
import tk.homevault.main.R;
import tk.homevault.main.ui.gallery.backup.BackupActivity;

public class GalleryFragment extends Fragment {

    private static final String PREF_SERVERIP = "serverip";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERROLE = "userrole";

    private String serverip;
    private String username;
    private String password;
    private String userrole;
    private String directory = "/";
    private String jsonPics;
    private InputStream downloadStream;

    private GridView gallery;
    private ArrayList<String> images;

    SwipeRefreshLayout refreshLayout;
    PhotoFetchActivity fetchPhotos;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        setHasOptionsMenu(true);
        refreshLayout = root.findViewById(R.id.swiperefresh);

        gallery = root.findViewById(R.id.galleryGridView);

        SharedPreferences pref = getActivity().getSharedPreferences("core_auth", Context.MODE_PRIVATE);
        serverip = pref.getString(PREF_SERVERIP, null);
        username = pref.getString(PREF_USERNAME, null);
        password = pref.getString(PREF_PASSWORD, null);
        userrole = pref.getString(PREF_USERROLE, null);

        FloatingActionMenu fab = ((MainActivity) getActivity()).fab;
        fab.setVisibility(View.GONE);

        fetchPhotos = new PhotoFetchActivity(getActivity(), this);
        fetchPhotos.execute(serverip, username, password, directory);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPhotos.cancel(true);
                fetchPhotos = new PhotoFetchActivity(getActivity(), GalleryFragment.this);
                fetchPhotos.execute(serverip, username, password, directory);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gallery_main2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_backup :
                startActivity(new Intent(getActivity(), BackupActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void rvSetup(String jsonResult) {
        jsonPics = jsonResult;

        gallery.setAdapter(new ImageAdapter(getActivity()));

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty()) {
                    ImageView imageView = arg1.findViewById(R.id.grid_pic);
                    imageView.setAlpha(0.5f);
                    new PhotoDownloadActivity(getActivity(), GalleryFragment.this, imageView).execute(serverip, username, password, "/../photos/" + images.get(position));
                }

            }
        });

        refreshLayout.setRefreshing(false);
    }

    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        /** The context. */
        private Activity context;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext
         *            the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setId(R.id.grid_pic);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(266, 266));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load("http://" + serverip + "/users/" + username + "/photos/" + images.get(position))
                    .placeholder(new ColorDrawable(0x66DDDDDD)).centerCrop()
                    .into(picturesView);

            return picturesView;
        }

        /**
         * Getting All Images Path.
         *
         * @param activity
         *            the activity
         * @return ArrayList with images Path
         */
        private ArrayList<String> getAllShownImagesPath(Activity activity) {
            ArrayList<String> fileNames = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(jsonPics);
                for (int i=0; i<jsonArray.length(); i++) {
                    fileNames.add(jsonArray.get(i).toString());
                }
            } catch(Exception ex) {
                Log.d("gallery_exception", ex.getMessage() + "\n" + ex.getStackTrace().toString() + "\n" + ex.getStackTrace().toString());
            }

            return fileNames;
        }


    }
}
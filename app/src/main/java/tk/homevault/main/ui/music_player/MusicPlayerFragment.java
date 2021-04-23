package tk.homevault.main.ui.music_player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import tk.homevault.main.MainActivity;
import tk.homevault.main.R;

public class MusicPlayerFragment extends Fragment {

    private FloatingActionMenu fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music_player, container, false);
        //final TextView textView = root.findViewById(R.id.text_slideshow);

        //textView.setText(getString(R.string.coming_soon));


        fab = ((MainActivity) getActivity()).fab;
        fab.setVisibility(View.GONE);
        return root;
    }
}
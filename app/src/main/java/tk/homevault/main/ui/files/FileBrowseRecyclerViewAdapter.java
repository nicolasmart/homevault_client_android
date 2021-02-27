package tk.homevault.main.ui.files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import tk.homevault.main.R;

public class FileBrowseRecyclerViewAdapter extends RecyclerView.Adapter<FileBrowseRecyclerViewAdapter.ViewHolder> {

    private List<String> mFolders;
    private List<String> mFiles;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private Integer folderCount;
    private Boolean backArrow;

    FileBrowseRecyclerViewAdapter(Context context, List<String> folders, List<String> files, Boolean backArrow) {
        this.mInflater = LayoutInflater.from(context);
        this.mFolders = folders;
        this.mFiles = files;
        this.folderCount = folders.size();
        this.backArrow = backArrow;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_file_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name;
        if (position < folderCount) {
            holder.itemName.setText(mFolders.get(position));
            if (position == 0 && backArrow) holder.itemType.setImageResource(R.drawable.ic_back_arrow);
            else holder.itemType.setImageResource(R.drawable.ic_folder);
        }
        else {
            holder.itemName.setText(mFiles.get(position - folderCount));
            holder.itemType.setImageResource(R.drawable.ic_file);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mFolders.size()+mFiles.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView itemName;
        ImageView itemType;
        ProgressBar downloadBar;

        ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.text_filename);
            itemType = itemView.findViewById(R.id.type_icon);
            downloadBar = itemView.findViewById(R.id.download_progress);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) return mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return (id>=folderCount) ? mFiles.get(id-folderCount) : mFolders.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}

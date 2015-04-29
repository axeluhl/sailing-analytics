package com.sap.sailing.racecommittee.app.ui.adapters.finishing;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class FinishListPhotoAdapter extends RecyclerView.Adapter<FinishListPhotoAdapter.ViewHolder> {

    private List<Uri> mUris;
    private float mHeight;
    private SimpleDateFormat mDateFormat;
    private Context mContext;

    public FinishListPhotoAdapter(List<Uri> uriList) {
        mUris = uriList;
    }

    @Override
    public FinishListPhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);

        mContext = parent.getContext();
        mHeight = parent.getMeasuredHeight();
        mDateFormat = new SimpleDateFormat("dd.MM.yyyy / kk:mm:ss", parent.getResources().getConfiguration().locale);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(FinishListPhotoAdapter.ViewHolder holder, int position) {
        String fileName = mUris.get(position).getEncodedPath();

        File file = new File(fileName);

        holder.dateTime.setText(mDateFormat.format(file.lastModified()));
        Bitmap bitmap = BitmapHelper.decodeSampleBitmapFromFile(fileName, (int) mHeight, (int) mHeight);
        holder.imageView.setImageBitmap(bitmap);
        holder.deleteView.setOnClickListener(new DeleteListener(file));

        holder.itemView.setTag(fileName);
    }

    @Override
    public int getItemCount() {
        return mUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTime;
        public ImageView imageView;
        public ImageView deleteView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTime = (TextView) itemView.findViewById(R.id.date_time);
            imageView = (ImageView) itemView.findViewById(R.id.photo_view);
            deleteView = (ImageView) itemView.findViewById(R.id.delete_photo);
        }
    }

    private class DeleteListener implements View.OnClickListener {

        private final File file;

        public DeleteListener(File file) {
            this.file = file;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_AlertDialog);
            builder.setTitle(mContext.getString(R.string.delete_file_title));
            builder.setMessage(mContext.getString(R.string.delete_file));
            builder.setNegativeButton(android.R.string.no, null);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mUris.remove(Uri.fromFile(file))) {
                        if (file.delete()) {
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            builder.create().show();
        }
    }
}

package com.sap.sailing.racecommittee.app.ui.adapters.finishing;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

import java.util.List;

public class FinishListPhotoAdapter extends RecyclerView.Adapter<FinishListPhotoAdapter.ViewHolder> {

    private List<Uri> mUris;

    public FinishListPhotoAdapter(List<Uri> uriList) {
        mUris = uriList;
    }

    @Override
    public FinishListPhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(FinishListPhotoAdapter.ViewHolder holder, int position) {
        Uri item = mUris.get(position);

        holder.dateTime.setText("01.04.2015 / 23:23:12");
        holder.imageView.setImageBitmap(BitmapHelper.decodeSampleBitmapFromFile(item.getEncodedPath(), 200, 200));

        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return mUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTime;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTime = (TextView) itemView.findViewById(R.id.date_time);
            imageView = (ImageView) itemView.findViewById(R.id.photo_view);
        }
    }
}

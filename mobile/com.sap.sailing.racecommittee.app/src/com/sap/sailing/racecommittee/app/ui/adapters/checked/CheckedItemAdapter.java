package com.sap.sailing.racecommittee.app.ui.adapters.checked;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

public class CheckedItemAdapter extends ArrayAdapter<CheckedItem> {

    private int checkedPosition;

    // Needs an unchecked cast back to base class. Won't compile otherwise.
    @SuppressWarnings("unchecked")
    public CheckedItemAdapter(Context context, List<? extends CheckedItem> items) {
        super(context, R.layout.checked_list_item, (List<CheckedItem>) items);
        checkedPosition = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.checked_list_item, parent, false);
        }

        CheckedItem item = getItem(position);
        ImageView itemImageView = ViewHelper.get(convertView, R.id.checked_item_image);
        TextView mainTextView = ViewHelper.get(convertView, R.id.list_item);
        TextView subTextView = ViewHelper.get(convertView, R.id.list_item_subtitle);
        ImageView checkImageView = ViewHelper.get(convertView, R.id.checked);

        if (item.getImage() != null && itemImageView != null) {
            BitmapHelper.setBackground(itemImageView, item.getImage());
            itemImageView.setVisibility(View.VISIBLE);
        }

        mainTextView.setText(item.getText());
        if (checkedPosition != -1) {
            if (position != checkedPosition) {
                mainTextView.setAlpha(0.2f);
                mainTextView.setTypeface(Typeface.DEFAULT);
                checkImageView.setVisibility(View.GONE);
            } else {
                mainTextView.setAlpha(1.0f);
                mainTextView.setTypeface(Typeface.DEFAULT_BOLD);
                checkImageView.setVisibility(View.VISIBLE);
            }
        }

        subTextView.setAlpha(1.0f);
        if (!TextUtils.isEmpty(item.getSubtext())) {
            subTextView.setText(item.getSubtext());
            subTextView.setVisibility(View.VISIBLE);
            mainTextView.setAlpha(1.0f);
        } else {
            subTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setCheckedPosition(int position) {
        checkedPosition = position;
    }
}

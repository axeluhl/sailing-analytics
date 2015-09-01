package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

public class CheckedItemListAdapter extends ArrayAdapter<CheckedListItem> {

    private int checkedPosition;

    public CheckedItemListAdapter(Context context, List<? extends CheckedListItem> items) {
        super(context, R.layout.checked_list_item, items);
        checkedPosition = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            convertView = inflater.inflate(R.layout.checked_list_item, parent, false);
        }
        CheckedListItem item = getItem(position);
        ImageView itemImageView = ViewHelper.get(convertView, R.id.checked_item_image);
        TextView mainTextView = ViewHelper.get(convertView, R.id.list_item);
        TextView subTextView = ViewHelper.get(convertView, R.id.list_item_subtitle);
        ImageView checkImageView = ViewHelper.get(convertView, R.id.checked);

        if (item.getImage() != null){
            BitmapHelper.setBackground(itemImageView, item.getImage());
        } else {
            itemImageView.setVisibility(View.GONE);
        }

        mainTextView.setText(item.getText());
        if (checkedPosition != -1 && position != checkedPosition) {
            mainTextView.setAlpha(0.2f);
        }

        if (item.getSubtext() != null && !item.getSubtext().equals("")) {
            subTextView.setText(item.getSubtext());
        } else {
            subTextView.setVisibility(View.GONE);
        }

        if (position != checkedPosition) {
            checkImageView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public void setCheckedPostion(int postion) {
        checkedPosition = postion;
    }
}

package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
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

    // Needs an unchecked cast back to base class. Won't compile otherwise.
    @SuppressWarnings("unchecked")
    public CheckedItemListAdapter(Context context, List<? extends CheckedListItem> items) {
        super(context, R.layout.checked_list_item, (List<CheckedListItem>) items);
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
            itemImageView.setVisibility(View.VISIBLE);
        }

        mainTextView.setText(item.getText());
        if (checkedPosition != -1 && position != checkedPosition) {
            mainTextView.setAlpha(0.2f);
            mainTextView.setTypeface(Typeface.DEFAULT);
        }else if (checkedPosition != -1 && position == checkedPosition) {
            mainTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mainTextView.setAlpha(1.0f);
            subTextView.setAlpha(1.0f);
            checkImageView.setVisibility(View.VISIBLE);
        }

        if (item.getSubtext() != null && !item.getSubtext().equals("")) {
            subTextView.setText(item.getSubtext());
            subTextView.setAlpha(0.2f);
            subTextView.setVisibility(View.VISIBLE);
        } else {
            subTextView.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setCheckedPostion(int postion) {
        checkedPosition = postion;
    }
}

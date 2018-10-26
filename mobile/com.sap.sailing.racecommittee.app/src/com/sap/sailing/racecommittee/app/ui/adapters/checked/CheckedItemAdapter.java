package com.sap.sailing.racecommittee.app.ui.adapters.checked;

import java.util.List;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
                if (item.isEnabled()) {
                    mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.sap_light_gray));
                } else {
                    mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray_white_10));
                }
                mainTextView.setTypeface(Typeface.DEFAULT);
                checkImageView.setVisibility(View.GONE);
            } else {
                mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.white));
                mainTextView.setTypeface(Typeface.DEFAULT_BOLD);
                checkImageView.setVisibility(View.VISIBLE);
            }
        } else if (!item.isEnabled()) {
            mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray_white_10));
        } else {
            mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.white));
        }

        if (!TextUtils.isEmpty(item.getSubtext())) {
            subTextView.setText(item.getSubtext());
            subTextView.setTextColor(ThemeHelper.getColor(getContext(),
                    position == checkedPosition ? R.attr.white : R.attr.sap_light_gray));
            subTextView.setVisibility(View.VISIBLE);
            mainTextView.setTextColor(ThemeHelper.getColor(getContext(), R.attr.white));
        } else {
            subTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    public void setCheckedPosition(int position) {
        checkedPosition = position;
    }
}

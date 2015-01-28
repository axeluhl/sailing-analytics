package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.Named;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author D053502
 */
public class NamedArrayAdapter<T extends Named> extends ArrayAdapter<T> {

    int isChecked = -1;

    public NamedArrayAdapter(Context context, List<T> namedList) {
        super(context, 0, namedList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.login_list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.list_item);
            holder.subText = (TextView) convertView.findViewById(R.id.list_item_subtitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        T item = getItem(position);

        if (holder.text != null) {
            holder.text.setText(item.getName());
            holder.text.setAlpha(isEnabled(position) ? 1.0f : 0.2f);
            if (isChecked == position) {
                holder.text.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                holder.text.setTypeface(Typeface.DEFAULT);
            }
        }

        if (holder.subText != null) {
            holder.subText.setVisibility(View.GONE);
        }

        if (holder.subText != null && item instanceof EventBase) {
            EventBase eventBase = (EventBase) item;
            String dateString = null;
            if (eventBase.getStartDate() != null && eventBase.getEndDate() != null) {
                Locale locale = getContext().getResources().getConfiguration().locale;
                Calendar startDate = Calendar.getInstance();
                startDate.setTime(eventBase.getStartDate().asDate());
                Calendar endDate = Calendar.getInstance();
                endDate.setTime(eventBase.getEndDate().asDate());
                String start = String.format("%s %s", startDate.getDisplayName(Calendar.MONTH, Calendar.LONG, locale), startDate.get(Calendar.DATE));
                String end = "";
                if (startDate.get(Calendar.MONTH) != endDate.get(Calendar.MONTH)) {
                    end = endDate.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
                }
                if (startDate.get(Calendar.DATE) != endDate.get(Calendar.DATE)) {
                    end += " " + endDate.get(Calendar.DATE);
                }
                dateString = String.format("%s %s %s", start , (!TextUtils.isEmpty(end.trim())) ? "-" : "", end.trim());
            }
            holder.subText.setText(String.format("%s %s %s", eventBase.getVenue().getName().trim(), (!TextUtils.isEmpty(dateString) ? ", " : ""), (!TextUtils.isEmpty(dateString) ? dateString : "")));
            holder.subText.setAlpha(isEnabled(position) ? 1.0f : 0.2f);
            holder.subText.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public void setSelected(int index) {
        isChecked = index;
    }

    static class ViewHolder {
        TextView text;
        TextView subText;
    }
}



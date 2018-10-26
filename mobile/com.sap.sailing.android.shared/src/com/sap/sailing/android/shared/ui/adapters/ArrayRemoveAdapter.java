package com.sap.sailing.android.shared.ui.adapters;

import java.util.List;

import com.sap.sailing.android.shared.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ArrayRemoveAdapter<T> extends ArrayAdapter<T> {

    public interface NotifyDataSetChangedListener<T> {
        void onNotifyDataSetChanged(ArrayRemoveAdapter<T> adapter);
    }

    private NotifyDataSetChangedListener<T> listener;

    public ArrayRemoveAdapter(Context context, List<T> objects) {
        super(context, R.layout.array_remove_adapter, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.array_remove_adapter, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        ImageButton button = (ImageButton) convertView.findViewById(R.id.array_remove_adapter_remove);

        final T item = getItem(position);
        text.setText(item.toString());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(item);
            }
        });
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (listener != null) {
            listener.onNotifyDataSetChanged(this);
        }
    }

    public NotifyDataSetChangedListener<T> getDataSetChangedListener() {
        return listener;
    }

    public void setDataSetChangedListener(NotifyDataSetChangedListener<T> listener) {
        this.listener = listener;
    }

}

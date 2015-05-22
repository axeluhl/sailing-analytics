package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends ArrayAdapter<Uri> {

    public PhotoAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        
        final Uri photoUri = getItem(position);
        
        if (row == null) {
            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.photo_adapter_item, parent, false);
        }
        
        TextView nameText = (TextView) row.findViewById(R.id.photo_adapter_name);
        String fullPath = photoUri.getPath();
        String[] pathSegments = fullPath.split("/");
        nameText.setText(pathSegments[pathSegments.length - 1]);
        
        ImageButton thumbnail = (ImageButton) row.findViewById(R.id.photo_adapter_thumbnail);
        thumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage(photoUri);
            }
        });

        ImageButton removeButton = (ImageButton) row.findViewById(R.id.photo_adapter_remove);
        removeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(photoUri);
            }
        });

        return (row);
    }

    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<Uri> getItems() {
        List<Uri> items = new ArrayList<Uri>();
        for (int i = 0; i < getCount(); i++) {
            items.add(getItem(i));
        }
        return items;
    }

    private void showImage(final Uri photoUri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(photoUri, "image/*");
        getContext().startActivity(intent);
    }

}

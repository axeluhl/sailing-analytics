package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.Named;

/**
 * @author D053502
 * 
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
            convertView = inflater.inflate(R.layout.login_list_item, /* view group */null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.txt_list_item);
            holder.check = (ImageView) convertView.findViewById(R.id.iv_check);
            
            convertView.setTag(holder);
        } else {
        	holder = (ViewHolder) convertView.getTag();
        }

        T item = getItem(position);
        
        
        holder.text.setText(item.getName());
        if ( isChecked == position ){
        	holder.text.setTypeface(Typeface.DEFAULT_BOLD);
        	holder.check.setVisibility(View.VISIBLE);
        } else {
        	holder.text.setTypeface(Typeface.DEFAULT);
        	holder.check.setVisibility(View.INVISIBLE);
        }
        
        return convertView;
    }

    public void setSelected(int index){
    	isChecked = index;
    }
    
    static class ViewHolder {
    	  TextView text;
    	  ImageView check;
    	}
    
}



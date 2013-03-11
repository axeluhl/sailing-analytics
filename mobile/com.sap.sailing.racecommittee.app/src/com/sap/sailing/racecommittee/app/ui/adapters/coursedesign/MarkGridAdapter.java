package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.R;

public class MarkGridAdapter extends ArrayAdapter<Mark> {

	public MarkGridAdapter(Context context, int textViewResourceId, List<Mark> marks) {
		super(context, textViewResourceId, marks);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {

			LayoutInflater li = (LayoutInflater) (getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
			view = li.inflate(R.layout.welter_grid_mark_cell, null);
		}

		Mark mark = getItem(position);
		
		//TODO: First test the setup just with names, then we will bring images in the app
//		ImageView image = (ImageView) view.findViewById(R.id.Welter_Grid_Mark_Cell_imgImage);
//		
//		if (mark.getImage() != null) {
//			Bitmap bitmap = BitmapFactory.decodeByteArray(mark.getImage(), 0, mark.getImage().length);
//			image.setImageBitmap(bitmap);
//		}
		
		TextView title = (TextView) view
				.findViewById(R.id.Welter_Grid_Mark_Cell_txtTitle);
		title.setText(mark.getName());

		return view;
	}
	
}

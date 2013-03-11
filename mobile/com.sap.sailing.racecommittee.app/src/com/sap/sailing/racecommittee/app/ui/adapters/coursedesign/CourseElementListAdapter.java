package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.RoundingDirection;

public class CourseElementListAdapter extends ArrayAdapter<CourseListDataElement> {
	
	public CourseElementListAdapter(Context context, int textViewResourceId, List<CourseListDataElement> objects) {
		super(context, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {

			LayoutInflater li = (LayoutInflater) (getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
			view = li.inflate(R.layout.welter_one_row_three_columns, null);
		}

		CourseListDataElement courseElement = getItem(position);
		
		TextView leftBuoyText = (TextView) view
				.findViewById(R.id.Welter_Cell_OneRowThreeColumns_columnOne_txtLeftMark);
		TextView roundingDirectionText = (TextView) view
				.findViewById(R.id.Welter_Cell_OneRowThreeColumns_txtRoundingDirection);
		TextView rightBuoyText = (TextView) view
				.findViewById(R.id.Welter_Cell_OneRowThreeColumns_columnThree_txtRightMark);
		
		ImageView leftBuoyImage = (ImageView) view.findViewById(R.id.Welter_Cell_OneRowThreeColumns_columnOne_imgImage);
		ImageView rightBuoyImage = (ImageView) view.findViewById(R.id.Welter_Cell_OneRowThreeColumns_columnThree_imgImage);
		
		leftBuoyText.setText(courseElement.getLeftMark().getName());
		leftBuoyImage.setVisibility(View.INVISIBLE);
		
//		if (courseElement.getLeftMark().getImage() != null) {
//			Bitmap bitmap = BitmapFactory.decodeByteArray(courseElement.getLeftMark().getImage(), 0, courseElement.getLeftMark().getImage().length);
//			leftBuoyImage.setImageBitmap(bitmap);
//		}
		
		if (courseElement.getRoundingDirection() != null)
			roundingDirectionText.setText(getDisplayValueForRounding(courseElement.getRoundingDirection()));
		else
			roundingDirectionText.setText(R.string.empty);
		
		if (courseElement.getRightMark() != null) {
			rightBuoyText.setText(courseElement.getRightMark().getName());
			
//			if (courseElement.getRightMark().getImage() != null) {
//				Bitmap bitmap = BitmapFactory.decodeByteArray(courseElement.getRightMark().getImage(), 0, courseElement.getRightMark().getImage().length);
//				rightBuoyImage.setVisibility(View.VISIBLE);
//				rightBuoyImage.setImageBitmap(bitmap);
//			}
		} else {
			rightBuoyText.setText(R.string.empty);
			rightBuoyImage.setVisibility(View.INVISIBLE);
		}
			

		return view;
	}
	
	protected String getDisplayValueForRounding(RoundingDirection direction) {
		if (direction.equals(RoundingDirection.Gate))
			return "Gate";
		else if (direction.equals(RoundingDirection.Port))
			return "P";
		else if (direction.equals(RoundingDirection.Starboard))
			return "S";
		
		return"";
	}

}

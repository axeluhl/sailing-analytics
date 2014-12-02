package com.sap.sailing.android.tracking.app.customviews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

public class OpenSansToolbar extends Toolbar {

	public OpenSansToolbar(Context context) {
		super(context);
		init();
	}

	public OpenSansToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public OpenSansToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	protected void init() {
		TextView titleTextView = getActionBarTextView();
		if (titleTextView != null)
		{
			System.out.println("NOT NULL");
			titleTextView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf"));
		}
		System.out.println("NULL");
	}

	private TextView getActionBarTextView() {
		TextView titleTextView = null;

		ArrayList<Field> fields = getAllFields(new ArrayList<Field>(), this.getClass());
	    for (Field fld : fields)
	      System.out.println(" " + fld);
	    
		try {
			Field f = this.getClass().getSuperclass().getDeclaredField("android.widget.TextView.android.support.v7.widget.Toolbar.mTitleTextView");
			f.setAccessible(true);
			titleTextView = (TextView) f.get(this);
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
		return titleTextView;
	}
	
	public static ArrayList<Field> getAllFields(ArrayList<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}

}

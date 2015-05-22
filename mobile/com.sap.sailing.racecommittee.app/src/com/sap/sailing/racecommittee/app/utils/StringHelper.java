package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;

public class StringHelper {

    private Context mContext;

    private StringHelper(Context context) {
        mContext = context;
    }

    public static StringHelper on(Context context) {
        StringHelper helper = new StringHelper(context);
        return helper;
    }

    public String getAuthor(String author) {
        if (AppConstants.AUTHOR_TYPE_OFFICER_START.equals(author)) {
            author = mContext.getString(R.string.author_type_officer_start);
        } else if (AppConstants.AUTHOR_TYPE_OFFICER_FINISH.equals(author)) {
            author = mContext.getString(R.string.author_type_officer_finish);
        } else if (AppConstants.AUTHOR_TYPE_SHORE_CONTROL.equals(author)) {
            author = mContext.getString(R.string.author_type_shore_control);
        } else if (AppConstants.AUTHOR_TYPE_VIEWER.equals(author)) {
            author = mContext.getString(R.string.author_viewer);
        }
        return author;
    }
}

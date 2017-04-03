package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.racecommittee.app.R;

public class SearchView extends FrameLayout {

    private View mSearchButton;
    private ImageView mSearchIcon;
    private EditText mEditText;

    private boolean mCollapsed = true;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View layout = LayoutInflater.from(context).inflate(R.layout.search_view, this, false);

        mEditText = (EditText) layout.findViewById(R.id.search_input);
        mSearchIcon = (ImageView) layout.findViewById(R.id.search_icon);
        mSearchButton = layout.findViewById(R.id.search_button);
        if (mSearchButton != null) {
            mSearchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickButton();
                }
            });
        }

        addView(layout);
    }

    private void onClickButton() {
        if (mCollapsed) {
            mSearchIcon.setImageDrawable(BitmapHelper.getAttrDrawable(getContext(), R.attr.clear_24dp));
            mEditText.setVisibility(VISIBLE);
        } else {
            mSearchIcon.setImageDrawable(BitmapHelper.getAttrDrawable(getContext(), R.attr.search_24dp));
            mEditText.setVisibility(GONE);
        }
        mCollapsed = !mCollapsed;
    }
}

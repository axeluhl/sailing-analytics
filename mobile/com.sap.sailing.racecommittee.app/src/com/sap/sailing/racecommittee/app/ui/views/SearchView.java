package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.racecommittee.app.R;

public class SearchView extends FrameLayout {

    private ImageView mSearchIcon;
    private EditText mEditText;
    private SearchTextWatcher mWatcher;

    private boolean mCollapsed = true;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View layout = LayoutInflater.from(context).inflate(R.layout.search_view, this, false);

        mEditText = (EditText) layout.findViewById(R.id.search_input);
        if (mEditText != null) {
            mEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mWatcher != null) {
                        mWatcher.onTextChanged(s.toString());
                    }
                }
            });
        }
        mSearchIcon = (ImageView) layout.findViewById(R.id.search_icon);
        View searchButton = layout.findViewById(R.id.search_button);
        if (searchButton != null) {
            searchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickButton();
                }
            });
        }

        addView(layout);
    }

    public void setSearchTextWatcher(SearchTextWatcher watcher) {
        mWatcher = watcher;
    }

    private void onClickButton() {
        if (mCollapsed) {
            mSearchIcon.setImageDrawable(BitmapHelper.getAttrDrawable(getContext(), R.attr.clear_24dp));
            mEditText.setVisibility(VISIBLE);
        } else {
            mSearchIcon.setImageDrawable(BitmapHelper.getAttrDrawable(getContext(), R.attr.search_24dp));
            mEditText.setText(null);
            mEditText.setVisibility(GONE);
        }
        mCollapsed = !mCollapsed;
    }

    public interface SearchTextWatcher {

        void onTextChanged(String text);

    }
}

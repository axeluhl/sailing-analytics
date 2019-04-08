package com.sap.sailing.android.tracking.app.ui.activities;

import com.sap.sailing.android.shared.ui.customviews.OpenSansToolbar;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.AboutFragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sap.sailing.android.shared.R.layout.fragment_container);

        OpenSansToolbar toolbar = (OpenSansToolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.hideSubtitle();
            toolbar.setTitleSize(20);
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64dp);
            int sidePadding = (int) getResources().getDimension(R.dimen.toolbar_left_padding);
            toolbar.setPadding(sidePadding, 0, 0, 0);
            ColorDrawable backgroundDrawable = new ColorDrawable(getResources().getColor(R.color.toolbar_background));
            getSupportActionBar().setBackgroundDrawable(backgroundDrawable);
            getSupportActionBar().setTitle(R.string.about_this_app);
        }
        replaceFragment(R.id.content_frame, AboutFragment.newInstance());
    }

    /**
     * Empty method to avoid creation of parent menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}

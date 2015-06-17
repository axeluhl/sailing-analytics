package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.PreferenceActivity;
import com.sap.sailing.racecommittee.app.ui.activities.SystemInformationActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.GeneralPreferenceFragment;

public class LoginBackdrop extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.login_backdrop, container, false);

        ImageView button = (ImageView) layout.findViewById(R.id.settings_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PreferenceActivity.class);
                    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
                    startActivity(intent);
                }
            });
        }

        ImageView info = (ImageView) layout.findViewById(R.id.technical_info);
        if (info != null) {
            info.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SystemInformationActivity.class);
                    startActivity(intent);
                }
            });
        }

        return layout;
    }
}

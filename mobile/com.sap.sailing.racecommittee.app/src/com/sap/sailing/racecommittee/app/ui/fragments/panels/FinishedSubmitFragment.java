package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ProtestTimeDialogFragment;
import com.sap.sailing.racecommittee.app.utils.CameraHelper;
import com.sap.sailing.racecommittee.app.utils.MailHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FinishedSubmitFragment extends BasePanelFragment {

    public static FinishedSubmitFragment newInstance(Bundle args) {
        FinishedSubmitFragment fragment = new FinishedSubmitFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_right, container, false);

        Button protest = ViewHolder.get(layout, R.id.protest_button);
        if (protest != null) {
            protest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProtestTimeDialogFragment fragment = ProtestTimeDialogFragment.newInstance(getRace());
                    fragment.show(getFragmentManager(), null);
                }
            });
        }

        Button submit = ViewHolder.get(layout, R.id.submit_button);
        if (submit != null) {
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String recipient = AppPreferences.on(getActivity()).getMailRecipient();
                    CharSequence subject = getSubject();
                    CharSequence body = getBody();
                    MailHelper.send(new String[] { recipient }, subject.toString(), body.toString(), getPhotos(), getActivity());
                }
            });
        }

        return layout;
    }

    public ArrayList<Uri> getPhotos() {
        ArrayList<Uri> retValue = new ArrayList<>();
        CameraHelper cameraHelper = CameraHelper.on(getActivity());
        File folder = cameraHelper.getOutputMediaFolder(cameraHelper.getSubFolder(getRace()));
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".mp4")) {
                retValue.add(Uri.fromFile(file));
            }
        }
        Collections.sort(retValue, new Comparator<Uri>() {
            @Override
            public int compare(Uri lhs, Uri rhs) {
                return lhs.getEncodedPath().compareTo(rhs.getEncodedPath());
            }
        });
        return retValue;
    }

    public CharSequence getSubject() {
        CharSequence subject = "";
        RacingActivity activity = (RacingActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                subject = actionBar.getTitle();
            }
        }
        return subject;
    }

    public CharSequence getBody() {
        CharSequence body = "See the attachments\n\nSend from my RCApp - " + AppUtils.getBuildInfo(getActivity());
        return body;
    }
}

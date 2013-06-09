/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.MailHelper;

public class RRS26FinishedRaceFragment extends RaceFragment {

    TextView headerView;
    TextView startTimeView;
    TextView firstBoatFinishedView;
    TextView finishTimeView;
    TextView timeLimitView;
    TextView protestStartTimeView;

    File finisherImageFile;
    static int FINISHER_IMAGE_REQUEST_CODE = 1337;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rrs26_race_finished_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createFinisherImageFile();

        headerView = (TextView) getView().findViewById(R.id.textFinishedRace);
        startTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceStarted);
        firstBoatFinishedView = (TextView) getView().findViewById(R.id.textFirstBoatFinished);
        finishTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceEnded);
        timeLimitView = (TextView) getView().findViewById(R.id.textTimeLimit);
        protestStartTimeView = (TextView) getView().findViewById(R.id.textProtestStartTime);

        headerView.setText(getHeaderText());
        startTimeView.setText(getStartTimeText());
        firstBoatFinishedView.setText(getFirstBoatFinishedTimeText());
        finishTimeView.setText(getFinishTimeText());
        timeLimitView.setText(getTimeLimitText());
        protestStartTimeView.setText(getProtestStartTimeText());

        ImageButton cameraButton = (ImageButton) getView().findViewById(R.id.buttonCamera);
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(finisherImageFile));
                startActivityForResult(intent, FINISHER_IMAGE_REQUEST_CODE);
            }
        });
    }

    private void createFinisherImageFile() {
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + AppConstants.ApplicationFolder);
        imageDirectory.mkdirs();
        finisherImageFile = new File(imageDirectory, "image.jpg");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FINISHER_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String recipient = preferences.getString("mailRecipientPreference", getString(R.string.settings_advanced_mail_default));
                MailHelper.send(new String[] { recipient },
                        String.format("[Race Committee] %s", getRace().getId()),
                        String.format("Results for race %s are attached.", getRace().getId()),
                        Uri.fromFile(finisherImageFile), getActivity());
            }
        }
    }

    private CharSequence getProtestStartTimeText() {
        // TODO Currently no protest time...
        return getString(R.string.empty);
    }

    private TimePoint getTimeLimit() {
        TimePoint startTime = getRace().getState().getStartTime();
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    private CharSequence getTimeLimitText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_time_limit), getFormattedTime(timeLimit.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getFinishTimeText() {
        TimePoint finishTime = getRace().getState().getFinishedTime();
        if (finishTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_end_time),
                    getFormattedTime(finishTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getFirstBoatFinishedTimeText() {
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (firstBoatTime != null) {
            return String.format("%s %s", getString(R.string.race_first_boat_finished),
                    getFormattedTime(firstBoatTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getStartTimeText() {
        TimePoint startTime = getRace().getState().getStartTime();
        if (startTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_start_time),
                    getFormattedTime(startTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private String getHeaderText() {
        return String.format(String.valueOf(getText(R.string.race_finished_template)), getRace().getName());
    }

    private String getFormattedTime(Date time) {
        return getFormattedTimePart(time.getHours()) + ":" + getFormattedTimePart(time.getMinutes()) + ":"
                + getFormattedTimePart(time.getSeconds());
    }

    private String getFormattedTimePart(int timePart) {
        return (timePart < 10) ? "0" + timePart : String.valueOf(timePart);
    }

    @Override
    public void onStart() {
        super.onStart();
        ExLog.i(RRS26FinishedRaceFragment.class.getName(),
                String.format("Fragment %s is now shown", RRS26FinishedRaceFragment.class.getName()));
    }

}

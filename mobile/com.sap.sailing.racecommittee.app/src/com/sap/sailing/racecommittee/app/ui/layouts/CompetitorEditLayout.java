package com.sap.sailing.racecommittee.app.ui.layouts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.StringArraySpinnerAdapter;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class CompetitorEditLayout extends FrameLayout {

    private CompetitorResultWithIdImpl mCompetitor;
    private GregorianCalendar mCalendar;

    private Spinner mPosition;
    private Spinner mPenalty;
    private EditText mScore;
    private NumberPicker mHours;
    private NumberPicker mMinutes;
    private NumberPicker mSeconds;
    private EditText mComment;

    public CompetitorEditLayout(Context context, CompetitorResultWithIdImpl competitor, int currentPos, int maxPos) {
        super(context);
        init(competitor, currentPos, maxPos);
    }

    private void init(CompetitorResultWithIdImpl competitor, int currentPos, int maxPos) {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.race_tracking_list_competitor_edit, this, false);

        mCompetitor = competitor;

        mCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        if (mCompetitor.getFinishingTime() != null) {
            mCalendar.setTimeInMillis(mCompetitor.getFinishingTime().asMillis());
        }

        mHours = ViewHelper.get(layout, R.id.competitor_finish_time_hours);
        if (mHours != null) {
            formatPicker(mHours, 0, 23);
            mHours.setValue(mCalendar.get(Calendar.HOUR_OF_DAY));
        }

        mMinutes = ViewHelper.get(layout, R.id.competitor_finish_time_minutes);
        if (mMinutes != null) {
            formatPicker(mMinutes, 0, 59);
            mMinutes.setValue(mCalendar.get(Calendar.MINUTE));
        }

        mSeconds = ViewHelper.get(layout, R.id.competitor_finish_time_seconds);
        if (mSeconds != null) {
            formatPicker(mSeconds, 0, 59);
            mSeconds.setValue(mCalendar.get(Calendar.SECOND));
        }

        mPosition = ViewHelper.get(layout, R.id.competitor_position);
        if (mPosition != null) {
            StringArraySpinnerAdapter positionAdapter = new StringArraySpinnerAdapter(getPositionList(maxPos));
            mPosition.setAdapter(positionAdapter);
            mPosition.setOnItemSelectedListener(new SpinnerSelectedListener(positionAdapter));
            mPosition.setSelection(currentPos);
        }

        mPenalty = ViewHelper.get(layout, R.id.competitor_penalty);
        if (mPenalty != null) {
            StringArraySpinnerAdapter penaltyAdapter = new StringArraySpinnerAdapter(getAllMaxPointsReasons());
            mPenalty.setAdapter(penaltyAdapter);
            mPenalty.setOnItemSelectedListener(new SpinnerSelectedListener(penaltyAdapter));
            mPenalty.setSelection(penaltyAdapter.getPosition(mCompetitor.getMaxPointsReason().toString()));
        }

        mScore = ViewHelper.get(layout, R.id.competitor_score);
        if (mScore != null && mCompetitor.getScore() != null) {
            mScore.setText(String.format(Locale.US, "%f", mCompetitor.getScore()));
        }

        mComment = ViewHelper.get(layout, R.id.competitor_comment);
        if (mComment != null) {
            mComment.setText(mCompetitor.getComment());
        }

        addView(layout);
    }

    private String[] getAllMaxPointsReasons() {
        List<String> result = new ArrayList<>();
        for (MaxPointsReason reason : MaxPointsReason.values()) {
            result.add(reason.name());
        }
        return result.toArray(new String[result.size()]);
    }

    private String[] getPositionList(int maxPos) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i <= maxPos; i++) {
            result.add(String.format(Locale.US, "%d", i));
        }
        return result.toArray(new String[result.size()]);
    }

    public CompetitorResultWithIdImpl getValue() {
        int oneBaseRank = 0;
        if (mPosition != null) {
            oneBaseRank = mPosition.getSelectedItemPosition();
        }
        MaxPointsReason maxPointsReason = MaxPointsReason.NONE;
        if (mPenalty != null) {
            maxPointsReason = MaxPointsReason.valueOf((String) mPenalty.getSelectedItem());
        }
        Double score = null;
        if (mScore != null && !TextUtils.isEmpty(mScore.getText())) {
            score = Double.valueOf(mScore.getText().toString());
        }
        if (mHours != null) {
            mCalendar.set(Calendar.HOUR_OF_DAY, mHours.getValue());
        }
        if (mMinutes != null) {
            mCalendar.set(Calendar.MINUTE, mMinutes.getValue());
        }
        if (mSeconds != null) {
            mCalendar.set(Calendar.SECOND, mSeconds.getValue());
        }
        TimePoint finishingTime = new MillisecondsTimePoint(mCalendar.getTime());
        String comment = null;
        if (mComment != null) {
            comment = mComment.getText().toString();
        }
        CompetitorResult result = new CompetitorResultImpl(mCompetitor.getCompetitorId(), mCompetitor.getCompetitorDisplayName(), oneBaseRank, maxPointsReason, score, finishingTime, comment);
        return new CompetitorResultWithIdImpl(mCompetitor.getId(), result);
    }

    private void formatPicker(@Nullable NumberPicker picker, int minValue, int maxValue) {
        if (picker != null) {
            picker.setMinValue(minValue);
            picker.setMaxValue(maxValue);
            picker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int value) {
                    return String.format(Locale.US, "%02d", value);
                }
            });
            ThemeHelper.setPickerColor(getContext(), picker, getContext().getResources().getColor(R.color.dialog_color_text),
                    getContext().getResources().getColor(R.color.dialog_color_button));
        }
    }

    private static class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        private StringArraySpinnerAdapter mAdapter;

        public SpinnerSelectedListener(StringArraySpinnerAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mAdapter.setSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            mAdapter.setSelected(0);
        }
    }
}

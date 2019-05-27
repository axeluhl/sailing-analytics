package com.sap.sailing.racecommittee.app.ui.layouts;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.StringArraySpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.FullTimePickerDialog;
import com.sap.sailing.racecommittee.app.ui.utils.DecimalKeyListener;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class CompetitorEditLayout extends ScrollView
        implements DatePickerDialog.OnDateSetListener, FullTimePickerDialog.OnTimeSetListener {

    private static final String TAG = CompetitorEditLayout.class.getName();

    private CompetitorResultWithIdImpl mCompetitor;
    private GregorianCalendar mCalendar;

    private Spinner mPosition;
    private Spinner mPenalty;
    private EditText mScore;
    private EditText mComment;
    private TextView mFinishDate;
    private TextView mFinishTime;
    private FrameLayout mDelete;

    private boolean mRestricted;

    private DecimalFormat format = new DecimalFormat();

    public CompetitorEditLayout(Context context, CompetitorResultWithIdImpl competitor, int maxPos,
                                boolean hasWarning) {
        this(context, null, competitor, maxPos, true, hasWarning);
    }

    public CompetitorEditLayout(final Context context, final TimePoint startTime, CompetitorResultWithIdImpl competitor,
                                int maxPos, boolean restrictedView, boolean hasWarning) {
        super(context);

        mRestricted = restrictedView;
        mCompetitor = competitor;
        int layoutId;
        if (AppUtils.with(getContext()).isPhone()) {
            layoutId = R.layout.race_tracking_list_competitor_edit_small;
        } else {
            layoutId = R.layout.race_tracking_list_competitor_edit_normal;
        }
        View layout = LayoutInflater.from(getContext()).inflate(layoutId, this, false);

        setFillViewport(true);
        setPadding(0, getResources().getDimensionPixelSize(R.dimen.dialog_top_padding), 0, 0);

        View position = ViewHelper.get(layout, R.id.competitor_position_layout);
        if (position != null) {
            position.setVisibility(restrictedView ? GONE : VISIBLE);
        }
        View warning = ViewHelper.get(layout, R.id.competitor_warning_message);
        if (warning != null) {
            warning.setVisibility(hasWarning ? VISIBLE : GONE);
        }

        mCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        if (mCompetitor.getFinishingTime() != null) {
            mCalendar.setTimeInMillis(mCompetitor.getFinishingTime().asMillis());
        }
        final int initialYear = mCalendar.get(Calendar.YEAR);
        final int initialMonth = mCalendar.get(Calendar.MONTH);
        final int initialDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        final int initialHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        final int initialMinutes = mCalendar.get(Calendar.MINUTE);
        final int initialSecond = mCalendar.get(Calendar.SECOND);

        View timeLayout = ViewHelper.get(layout, R.id.competitor_finish_time_layout);
        if (timeLayout != null) {
            timeLayout.setVisibility(mRestricted ? GONE : VISIBLE);
        }
        mFinishDate = ViewHelper.get(layout, R.id.competitor_finish_date);
        if (mFinishDate != null) {
            mFinishDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = initialYear;
                    int month = initialMonth;
                    int day = initialDay;
                    if (mCalendar != null) {
                        year = mCalendar.get(Calendar.YEAR);
                        month = mCalendar.get(Calendar.MONTH);
                        day = mCalendar.get(Calendar.DAY_OF_MONTH);
                    }
                    DatePickerDialog dialog = new DatePickerDialog(context, R.style.AppTheme_PickerDialog,
                            CompetitorEditLayout.this, year, month, day);
                    dialog.getDatePicker().setMinDate(startTime.asMillis());
                    dialog.show();
                }
            });
        }
        mFinishTime = ViewHelper.get(layout, R.id.competitor_finish_time);
        if (mFinishTime != null) {
            mFinishTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int hour = initialHour;
                    int minutes = initialMinutes;
                    int second = initialSecond;
                    if (mCalendar != null) {
                        hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                        minutes = mCalendar.get(Calendar.MINUTE);
                        second = mCalendar.get(Calendar.SECOND);
                    }
                    FullTimePickerDialog dialog = new FullTimePickerDialog(context, R.style.AppTheme_PickerDialog,
                            CompetitorEditLayout.this, hour, minutes, second, true);
                    dialog.show();
                }
            });
        }

        if (mCompetitor.getFinishingTime() == null) {
            mCalendar = null;
        }

        mDelete = ViewHelper.get(layout, R.id.competitor_finish_date_delete);
        if (mDelete != null) {
            mDelete.setEnabled(false);
            mDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCalendar = null;
                    showFinishTime();
                }
            });
        }
        showFinishTime();

        mPosition = ViewHelper.get(layout, R.id.competitor_position);
        if (mPosition != null) {
            StringArraySpinnerAdapter positionAdapter = new StringArraySpinnerAdapter(getPositionList(maxPos));
            mPosition.setAdapter(positionAdapter);
            mPosition.setOnItemSelectedListener(new StringArraySpinnerAdapter.SpinnerSelectedListener(positionAdapter));
            mPosition.setSelection(competitor.getOneBasedRank());
        }
        mPenalty = ViewHelper.get(layout, R.id.competitor_penalty);
        if (mPenalty != null) {
            StringArraySpinnerAdapter penaltyAdapter = new StringArraySpinnerAdapter(getAllMaxPointsReasons());
            mPenalty.setAdapter(penaltyAdapter);
            mPenalty.setOnItemSelectedListener(new StringArraySpinnerAdapter.SpinnerSelectedListener(penaltyAdapter));
            mPenalty.setSelection(penaltyAdapter.getPosition(mCompetitor.getMaxPointsReason().toString()));
        }
        mScore = ViewHelper.get(layout, R.id.competitor_score);
        if (mScore != null) {
            mScore.setKeyListener(new DecimalKeyListener());
            final char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
            mScore.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mScore.removeTextChangedListener(this);
                    for (int i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        // If necessary replace '.' with ','
                        if (c == '.' && separator != '.') {
                            s.replace(i, i + 1, Character.toString(separator));
                        }
                    }
                    mScore.addTextChangedListener(this);
                }
            });
            if (mCompetitor.getScore() != null) {
                mScore.setText(format.format(mCompetitor.getScore()));
            }
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
            addPositionToPositionList(result, i);
        }
        return result.toArray(new String[result.size()]);
    }

    private void addPositionToPositionList(List<String> result, int i) {
        result.add(String.format(Locale.getDefault(), "%d", i));
    }

    public CompetitorResultWithIdImpl getValue() {
        int oneBaseRank = 0;
        MaxPointsReason maxPointsReason = MaxPointsReason.NONE;
        if (mPenalty != null) {
            maxPointsReason = MaxPointsReason.valueOf((String) mPenalty.getSelectedItem());
        }
        if (mPosition != null) {
            oneBaseRank = mPosition.getSelectedItemPosition();
        }
        Double score = null;
        if (mScore != null && !TextUtils.isEmpty(mScore.getText())) {
            try {
                score = format.parse(mScore.getText().toString()).doubleValue();
            } catch (ParseException e) {
                ExLog.w(getContext(), TAG, "Parsing score failed: " + mScore.getText().toString());
            }
        }
        TimePoint finishingTime = null;
        if (!mRestricted) {
            if (mCalendar != null) {
                finishingTime = new MillisecondsTimePoint(mCalendar.getTime());
            } else {
                finishingTime = null;
            }
        }
        String comment = null;
        if (mComment != null) {
            comment = mComment.getText().toString();
        }
        CompetitorResult result = new CompetitorResultImpl(mCompetitor.getCompetitorId(), mCompetitor.getName(),
                mCompetitor.getShortName(), mCompetitor.getBoatName(), mCompetitor.getBoatSailId(), oneBaseRank,
                maxPointsReason, score, finishingTime, comment, MergeState.OK);
        return new CompetitorResultWithIdImpl(mCompetitor.getId(), mCompetitor.getBoat(), result);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (mCalendar == null) {
            mCalendar = (GregorianCalendar) Calendar.getInstance();
        }
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        showFinishTime();
    }

    @Override
    public void onTimeSet(FullTimePickerDialog dialog, int hourOfDay, int minute, int second) {
        if (mCalendar == null) {
            mCalendar = (GregorianCalendar) Calendar.getInstance();
        }
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        showFinishTime();
    }

    private void showFinishTime() {
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        if (mFinishDate != null) {
            if (mCalendar != null) {
                mFinishDate.setText(date.format(mCalendar.getTime()));
            } else {
                mFinishDate.setText(null);
            }
        }
        if (mFinishTime != null) {
            if (mCalendar != null) {
                mFinishTime.setText(time.format(mCalendar.getTime()));
            } else {
                mFinishTime.setText(null);
            }
        }
        mDelete.setEnabled(mCalendar != null);
        for (int i = 0; i < mDelete.getChildCount(); i++) {
            mDelete.getChildAt(i).setEnabled(mDelete.isEnabled());
        }
    }

}

package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public class SetTimeRaceFragment extends RaceFragment implements TickListener {
	protected boolean isReset;

	protected TimePicker startTimePicker;
	protected Button setStartTimeButton;
	protected TextView countdownView;
	protected ImageButton abortRaceButton;
	
	protected Date scheduledTime;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.race_reset_time, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		isReset = getArguments().getBoolean(AppConstants.RESET_TIME_FRAGMENT_IS_RESET);

		startTimePicker = (TimePicker) getView().findViewById(R.id.timePicker);
		setStartTimeButton = (Button) getView().findViewById(R.id.btnRescheduleTime);
		countdownView = (TextView) getView().findViewById(R.id.time_below_picker);
		abortRaceButton = (ImageButton) getView().findViewById(R.id.resetTimeAPButton);
		
		startTimePicker.setIs24HourView(true);
		startTimePicker.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				refreshDifferenceTime();
			}
		});
		setStartTimeButton.setText(isReset ? R.string.reset_time : R.string.set_time);
		setStartTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				setStartTime();
			}
		});
		
		/// TODO: click-listener for abort button
		
		refreshTimePickerTime();
	}

	@Override
	public void onStart() {
		super.onStart();
		TickSingleton.INSTANCE.registerListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		TickSingleton.INSTANCE.unregisterListener(this);
	}

	public void notifyTick() {
		refreshDifferenceTime();
	}
	
	private void refreshTimePickerTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, AppConstants.DefaultStartTimeMinuteOffset);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		startTimePicker.setCurrentHour(hour);
		startTimePicker.setCurrentMinute(minute);
		refreshDifferenceTime();;
	}
	
	private void refreshDifferenceTime() {
		// This method might be called by the ticker before initialization happens...
		if (startTimePicker == null || countdownView == null) {
			return;
		}
		
		/// TODO: Why is this method synchronized?
		synchronized (this) {
			int hourOfDay = startTimePicker.getCurrentHour();
			int minute = startTimePicker.getCurrentMinute();

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			Date nowDate = calendar.getTime();

			Date pickedDate = (Date) nowDate.clone();
			pickedDate.setHours(hourOfDay);
			pickedDate.setMinutes(minute);
			pickedDate.setSeconds(0);

			long nowTime = nowDate.getTime();
			long pickedTime = pickedDate.getTime();
			long diffTime = pickedTime - nowTime;
			if (diffTime >= 0 && diffTime > 12 * 60 * 60 * 1000) {
				diffTime -= (24 * 60 * 60 * 1000);
			} else if (diffTime < 0 && diffTime < -(12 * 60 * 60 * 1000)) {
				diffTime += (24 * 60 * 60 * 1000);
			}

			long diffHours = diffTime / 1000 / 60 / 60;
			long diffMins = (diffTime / 1000 / 60) % 60;
			long diffSecs = (diffTime / 1000) % 60;

			String minusInd = (diffHours < 0 || diffMins < 0 || diffSecs < 0 ? "-" : "");

			countdownView.setText(getString(R.string.time_until_start) + ": "
					+ minusInd + Math.abs(diffHours) + "h "
					+ Math.abs(diffMins) + "min " + Math.abs(diffSecs) + "sec");
		}
	}
	
	protected void setStartTime() {
		if (isReset) {
			ExLog.i(ExLog.RACE_RESET_TIME, getRace().getId().toString(), getActivity());
		} else {
			ExLog.i(ExLog.RACE_SET_TIME, getRace().getId().toString(), getActivity());
		}
		
		/// TODO: need to open course designer?
		
		Calendar newTime = Calendar.getInstance();
		newTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getCurrentHour());
		newTime.set(Calendar.MINUTE, startTimePicker.getCurrentMinute());
		newTime.set(Calendar.SECOND, 0);
		newTime.set(Calendar.MILLISECOND, 0);

		Date currentTime = new Date();
		
		if (hasStartphaseAlreadyBegun(newTime.getTime(), currentTime)) {
			scheduledTime = newTime.getTime();
			/// TODO: do whatever is needed for already ongoing start phase
		}
		setStartTime(newTime.getTime());
	}
	
	protected boolean hasStartphaseAlreadyBegun(Date newStartTime, Date currentTime) {
		/// TODO: decicde whether start phase has already begun
		return false;
	}

	
	private void setStartTime(Date newStartTime) {
		/// TODO: set new start time on race
		//boolean success = getRace().setStartTime(newStartTime);
		boolean success = false;
		if (!success) {
			Toast.makeText(getActivity(), getString(R.string.race_settime_overlap), Toast.LENGTH_LONG).show();
		}
	}

	/*

	private boolean shouldStartModeAlreadyBeenSet(Date newTime, Date currentTime) {
		if (!race.usesRRS26Startprocedure()) {
			return false;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(newTime);
		// cal.add(Calendar.MINUTE, 0 -
		// AppConstants.STARTPHASE_DURATION_MINUTES);
		cal.add(Calendar.MINUTE,
				0 - AppConstants.STARTPHASE_STARTMODE_DISPLAYED_MINUTES_BEFORE_START);
		Date startModeDate = cal.getTime();

		return startModeDate.before(currentTime);
	}
	
	protected void showChooseCourseDesignDialog() {
		FragmentManager fragmentManager = getFragmentManager();

		RaceDialogFragment fragment = new RaceChooseCourseDesignDialog();

		Bundle args = getParameterBundle();
		fragment.setArguments(args);

		fragment.show(fragmentManager, "dialogCourseDesign");
	}

	protected void showChooseRunningCourceDialog() {
		FragmentManager fragmentManager = getFragmentManager();

		RaceDialogFragment fragment = new RaceChooseRunningCourseDialogExtended();

		Bundle args = getParameterBundle();
		fragment.setArguments(args);

		fragment.show(fragmentManager, "dialogRunningCourse");
	}

	private class RaceChooseRunningCourseDialogExtended extends
			RaceChooseRunningCourse {

		@Override
		protected OnClickListener getOnChooseClickListener() {
			return new OnClickListener() {

				public void onClick(View v) {
					try {
						int nmbrRounds = Integer.parseInt(numberOfRoundsEdit
								.getText().toString());
						Routes selectedRoutes = (Routes) routesSpinner
								.getAdapter()
								.getItem(
										routesSpinner.getSelectedItemPosition());
						sendChangedRunningCourse(nmbrRounds, selectedRoutes);
						ExLog.i(ExLog.RACE_SET_RACE_RUNNING_COURSE_REMINDER,
								String.valueOf(nmbrRounds) + ":"
										+ selectedRoutes.toString(),
								getActivity());
						resetStartTime();
						dismiss();
					} catch (NumberFormatException e) {
						ExLog.i(ExLog.RACE_SET_RACE_RUNNING_COURSE_REMINDER_FAIL,
								null, getActivity());
						Toast.makeText(getActivity(),
								"Please fill in the required fields.", Toast.LENGTH_LONG)
								.show();
					}
				}
			};
		}
	}*/

}

package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import com.sap.sailing.racecommittee.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RaceChooseLineOpeningTimeDialog extends RaceDialogFragment {
	
	public interface GateLineOpeningTimeSelectionListener {
		public void onLineOpeningTimeSelected();
	}

	private GateLineOpeningTimeSelectionListener selectionListener;
	
	private EditText lineOpeningTimeEditText;
	private Button chooseButton;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		selectionListener = (GateLineOpeningTimeSelectionListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.race_choose_line_opening_time_view, container);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getDialog().setTitle(getString(R.string.set_gate_line_opening_time_title));
		
		lineOpeningTimeEditText = (EditText) getView().findViewById(R.id.lineOpeningTimeText);
		chooseButton = (Button) getDialog().findViewById(R.id.chooseLineOpeningTimeButton);
		
		chooseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onChooseClicked(v);
			}
		});
	}
	
	protected void onChooseClicked(View view) {
		int lineOpeningTime = convertToInteger(lineOpeningTimeEditText.getText().toString());
		//this.getRace().changeGateLineOpeningTime(lineOpeningTime);
		selectionListener.onLineOpeningTimeSelected();
		Log.i("RACE_SET_GATELINE_OPENING_TIME", String.valueOf(lineOpeningTime));
		dismiss();
	}
	
	protected Integer convertToInteger(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("An NumberFormatException occured");
		}
	}

    @Override
    public void notifyTick() {
        // TODO Auto-generated method stub
        
    }
}

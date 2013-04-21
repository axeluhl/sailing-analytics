package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sap.sailing.racecommittee.app.R;

public class RaceChoosePathFinderDialog extends RaceDialogFragment {
	
	public interface PathfinderSelectionListener {
		public void onPathfinderSelected();
	}
	
	private EditText sailingNationalityEditText;
	private EditText sailingNumberEditText;
	private Button chooseButton;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//selectionListener = (PathfinderSelectionListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.race_choose_path_finder_view, container);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getDialog().setTitle(getString(R.string.set_pathfinder_title));
		
		sailingNationalityEditText = (EditText) getView().findViewById(R.id.pathFinderNationality);
		sailingNumberEditText = (EditText) getView().findViewById(R.id.pathFinderNumber);
		
		final EditText focusEditText = sailingNumberEditText;
		sailingNationalityEditText.addTextChangedListener(new TextWatcher() {
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {
                        if(s.length() == 3)
                            focusEditText.requestFocus();
                    }
                });
		
		chooseButton = (Button) getDialog().findViewById(R.id.choosePathFinderButton);
		
		chooseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onChooseClicked(v);
			}
		});
	}
	
	protected void onChooseClicked(View view) {
		String sailingId = sailingNationalityEditText.getText().toString() + " " + sailingNumberEditText.getText().toString();
		this.getRace().getState().setPathfinder(sailingId);
		Log.i("RACE_SET_PATHFINDER", sailingId);
		dismiss();
	}

    @Override
    public void notifyTick() {
        // TODO Auto-generated method stub
        
    }
	
}

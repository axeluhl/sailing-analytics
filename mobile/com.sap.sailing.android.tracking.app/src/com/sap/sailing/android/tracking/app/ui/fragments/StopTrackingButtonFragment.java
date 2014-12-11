package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.tracking.app.R;

public class StopTrackingButtonFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_stop_button, container,false);
		return view;
	}
}

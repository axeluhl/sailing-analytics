package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.ui.adapters.NamedArrayAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogFragmentButtonListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.FragmentDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoadFailedDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public abstract class NamedListFragment<T extends Named> extends ListFragment implements 
	LoadClient<Collection<T>>, DialogFragmentButtonListener {
	private ItemSelectedListener<T> listener;
	private NamedArrayAdapter<T> listAdapter;
	private ReadonlyDataManager dataManager;
	
	protected ArrayList<T> namedList;
	
	protected abstract ItemSelectedListener<T> attachListener(Activity activity);
	
	protected abstract String getHeaderText();
	
	protected abstract void loadItems(ReadonlyDataManager manager);
	
	protected NamedArrayAdapter<T> createAdapter(Context context, int layoutId, ArrayList<T> items) {
		return new NamedArrayAdapter<T>(context, layoutId, items);
	}
	
	protected void loadItems() {
		loadItems(dataManager);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.listener = attachListener(activity);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		addHeader();
		
		namedList = new ArrayList<T>();
		listAdapter = createAdapter(getActivity(), android.R.layout.simple_list_item_single_choice, namedList);
		
		this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		this.setListAdapter(listAdapter);
		
		showProgressBar(true);
		dataManager = OnlineDataManager.create(getActivity());
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// this unchecked cast here seems unavoidable.
		// even SDK example code does it...
		@SuppressWarnings("unchecked")
		T item = (T) l.getItemAtPosition(position);
		listener.itemSelected(this, item);
	}

	private void addHeader() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.selection_list_header_view, getListView(), false);
		getListView().addHeaderView(header, null, false);
		TextView textText = ((TextView)header.findViewById(R.id.textHeader));
		textText.setText(getHeaderText());
	}
	
	public void onLoadSucceded(Collection<T> data) {
		namedList.clear();
		namedList.addAll(data);
		Collections.sort(namedList, new NamedComparator());
		listAdapter.notifyDataSetChanged();
		
		showProgressBar(false);
		
		// we set this here to ensure that the screen is initially
		// not covered with the following text while loading
		//this.setEmptyText("There is no item available.");
	}
	
	public void onLoadFailed(Exception reason) {
		showProgressBar(false);
		
		String message = reason.getMessage();
		if (message == null) {
		    message = reason.toString();
		}
		showLoadFailedDialog(message);
	}

	private void showProgressBar(boolean visible) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.setProgressBarIndeterminateVisibility(visible);
		}
	}
	
	private void showLoadFailedDialog(String message) {
		FragmentDialogFragment dialog = LoadFailedDialog.create(message);
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), "failedDialog");
	}
	
	public void onDialogNegativeButton() { }
	
	public void onDialogPositiveButton() {
		loadItems();
	}
}
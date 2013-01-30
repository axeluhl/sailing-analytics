package com.sap.sailing.racecommittee.app.fragments.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.racecommittee.app.R;

public abstract class NamedListFragment<T extends Named> extends ListFragment/* implements BaseLoadedListener<T>*/ {
	
	public interface ItemSelectedListener<T> {
		public void itemSelected(Fragment sender, T event);
	}
	
	/*private ItemSelectedListener<T> listener;
	private NamedAdapter<T> listAdapter;
	protected ArrayList<T> namedList;
	private ResourceManager resourceManager;
	
	protected String getHeaderText() {
		return "Undefined";
	}
	
	abstract protected ItemSelectedListener<T> attachListener(Activity activity);
	
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
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		resourceManager = new ResourceManager(getActivity());
	}


	private void addHeader() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.selection_list_header_view, getListView(), false);
		getListView().addHeaderView(header, null, false);
		TextView textText = ((TextView)header.findViewById(R.id.textHeader));
		textText.setText(getHeaderText());
	}
	
	protected NamedAdapter<T> createAdapter(Context context, int layoutId, ArrayList<T> items) {
		return new NamedAdapter<T>(context, layoutId, items);
	}
	
	protected void loadItems() {
		loadItems(resourceManager);
	}
	
	protected abstract void loadItems(ResourceManager manager);
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// this unchecked cast here seems unavoidable.
		// even sdk example code does it...
		@SuppressWarnings("unchecked")
		T item = (T) l.getItemAtPosition(position);
		listener.itemSelected(this, item);
	}

	protected void onResourcesLoaded(Collection<T> aListOfNamed) {
		namedList.clear();
		namedList.addAll(aListOfNamed);
		Collections.sort(namedList, new NamedComparator());
		listAdapter.notifyDataSetChanged();
		Activity activity = getActivity();
		if (activity != null) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
		
		// we set this here to ensure that the screen is initially
		// not covered with the following text while loading
		//this.setEmptyText("There is no item available.");
	}
	
	public void onLoadFailed(Exception reason) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
		//this.setEmptyText("Load failure.");
		showLoadFailedDialog(reason.getMessage());
	}
	
	private void showLoadFailedDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); 
		builder.setMessage(String.format("There was an error loading the requested data: %s\nDo you want to retry?", message))
			   .setTitle("Load failure")
			   .setIcon(R.drawable.ic_dialog_alert_holo_light)
		       .setCancelable(true) 
		       .setPositiveButton("Retry", new DialogInterface.OnClickListener() { 
		           public void onClick(DialogInterface dialog, int id) { 
		        	   loadItems();
		           } 
		       }) 
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
		           public void onClick(DialogInterface dialog, int id) { 
		        	   dialog.cancel(); 
		           } 
		       }); 
		AlertDialog alert = builder.create(); 
		alert.show(); 
	}*/
}
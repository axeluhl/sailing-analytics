package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.adapters.NamedArrayAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AttachedDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.FragmentAttachedDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoadFailedDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sse.common.Named;

public abstract class NamedListFragment<T extends Named> extends LoggableListFragment implements LoadClient<Collection<T>>,
        DialogListenerHost {
    
    //private static String TAG = NamedListFragment.class.getName();
    
    private ItemSelectedListener<T> listener;
    private NamedArrayAdapter<T> listAdapter;
    private TextView txt_header;


    
    protected ArrayList<T> namedList;

    protected abstract ItemSelectedListener<T> attachListener(Activity activity);

    protected abstract String getHeaderText();

    protected NamedArrayAdapter<T> createAdapter(Context context, ArrayList<T> items) {
        return new NamedArrayAdapter<T>(context, items);
    }

    
    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }*/
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.list_fragment, container, false);
        parent.addView(v, 1);
        txt_header = (TextView) parent.findViewById(R.id.txt_listHeader);
        return parent;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = attachListener(activity);
    }
    
    protected abstract LoaderCallbacks<DataLoaderResult<Collection<T>>> createLoaderCallbacks(ReadonlyDataManager manager);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addHeader();

        namedList = new ArrayList<T>();
        listAdapter = createAdapter(getActivity(), /*android.R.layout.simple_list_item_single_choice, */namedList);

        this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.setListAdapter(listAdapter);

        showProgressBar(true);
        loadItems();
    }

    private void loadItems() {
        setListShown(false);
        getLoaderManager().restartLoader(0, null, createLoaderCallbacks(OnlineDataManager.create(getActivity())));
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

    protected void addHeader() {
    	txt_header.setText(getHeaderText());
    	txt_header.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadSucceded(Collection<T> data, boolean isCached) {
        setListShown(true);
        namedList.clear();
        namedList.addAll(data);
        Collections.sort(namedList, new NaturalNamedComparator());
        listAdapter.notifyDataSetChanged();

        showProgressBar(false);
    }

    @Override
    public void onLoadFailed(Exception reason) {
        setListShown(true);
        namedList.clear();
        listAdapter.notifyDataSetChanged();
        
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
        FragmentManager manager = getFragmentManager();
        FragmentAttachedDialogFragment dialog = LoadFailedDialog.create(message);
        dialog.setTargetFragment(this, 0);
        // We cannot use DialogFragment#show here because we need to commit the transaction
        // allowing a state loss, because we are effectively in Loader#onLoadFinished()
        manager.beginTransaction().add(dialog, "failedDialog").commitAllowingStateLoss();
    }
    
    @Override
    public DialogResultListener getListener() {
        return new DialogResultListener() {
            
            @Override
            public void onDialogPositiveButton(AttachedDialogFragment dialog) {
                loadItems();
            }
            
            @Override
            public void onDialogNegativeButton(AttachedDialogFragment dialog) {
                
            }
        };
    }
}
package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.ui.adapters.NamedArrayAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DialogListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.FragmentAttachedDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoadFailedDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.Named;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class NamedListFragment<T extends Named> extends LoggableListFragment
    implements LoadClient<Collection<T>>, DialogListenerHost {

    protected ArrayList<T> namedList;
    private ItemSelectedListener<T> listener;
    private NamedArrayAdapter<T> listAdapter;
    private View lastSelected;
    private int mSelectedIndex = -1;

    protected abstract ItemSelectedListener<T> attachListener(Activity activity);

    protected NamedArrayAdapter<T> createAdapter(Context context, ArrayList<T> items) {
        return new NamedArrayAdapter<>(context, items);
    }

    protected abstract LoaderCallbacks<DataLoaderResult<Collection<T>>> createLoaderCallbacks(ReadonlyDataManager manager);

    private void loadItems() {
        setListShown(false);
        setupLoader();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        namedList = new ArrayList<>();
        listAdapter = createAdapter(getActivity(), namedList);
        if (savedInstanceState != null) {
            mSelectedIndex = savedInstanceState.getInt("position", -1);
            if (mSelectedIndex >= 0) {
                listAdapter.setSelected(mSelectedIndex);

            }
        }
        this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.setListAdapter(listAdapter);
        getListView().setDivider(null);

        showProgressBar(true);
        loadItems();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = attachListener(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.list_fragment, container, false);
        parent.addView(view, 1);
        return parent;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        listAdapter.setSelected(position);
        setStyleClicked(view);

        mSelectedIndex = position;

        // this unchecked cast here seems unavoidable.
        // even SDK example code does it...
        @SuppressWarnings("unchecked")
        T item = (T) listView.getItemAtPosition(position);
        listener.itemSelected(this, item);
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

    @Override
    public void onLoadSucceeded(Collection<T> data, boolean isCached) {
        setListShown(true);
        namedList.clear();
        namedList.addAll(data);
        Collections.sort(namedList, new NaturalNamedComparator());
        listAdapter.notifyDataSetChanged();

        showProgressBar(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mSelectedIndex);

        Fragment fragment = getTargetFragment();
        if (fragment != null && !fragment.isAdded()) {
            setTargetFragment(null, -1);
            listener = null;
            listAdapter = null;
        }

        super.onSaveInstanceState(outState);
    }

    private void setStyleClicked(View view) {
        TextView textView;
        ImageView imageView;

        // reset last styles:
        if (lastSelected != null) {
            textView = (TextView) lastSelected.findViewById(R.id.list_item_subtitle);
            if (textView != null) {
                textView.setTextColor(ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray));
            }

            imageView = (ImageView) lastSelected.findViewById(R.id.checked);
            if (imageView != null) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        // set new styles
        textView = (TextView) view.findViewById(R.id.list_item_subtitle);
        if (textView != null) {
            textView.setTextColor(ThemeHelper.getColor(getActivity(), R.attr.white));
        }
        imageView = (ImageView) view.findViewById(R.id.checked);
        if (imageView != null) {
            imageView.setVisibility(View.VISIBLE);
        }

        lastSelected = view;
    }

    public void setupLoader() {
        getLoaderManager().restartLoader(0, null, createLoaderCallbacks(OnlineDataManager.create(getActivity())));
    }

    private void showLoadFailedDialog(String message) {
        FragmentManager manager = getFragmentManager();
        FragmentAttachedDialogFragment dialog = LoadFailedDialog.create(message);
        //FIXME this can't be the real solution for the autologin
        dialog.setTargetFragment(this, 0);
        // We cannot use DialogFragment#show here because we need to commit the transaction
        // allowing a state loss, because we are effectively in Loader#onLoadFinished()
        manager.beginTransaction().add(dialog, "failedDialog").commitAllowingStateLoss();
    }

    private void showProgressBar(boolean visible) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setProgressBarIndeterminateVisibility(visible);
        }
    }
}
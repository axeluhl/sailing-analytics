package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.LoginType;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItem;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;

public class PositionListFragment extends LoggableListFragment {

    private LogEventAuthorImpl author;
    private AppPreferences preferences;

    private PositionSelectedListenerHost host;

    public PositionListFragment() {
    }

    public static PositionListFragment newInstance() {
        return new PositionListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.list_fragment, container, false);
        if (view != null) {
            parent.addView(view, 1);
        }

        preferences = AppPreferences.on(getActivity());

        final ArrayList<String> values = new ArrayList<>();
        values.add(getString(R.string.login_type_officer_on_start_vessel));
        values.add(getString(R.string.login_type_officer_on_finish_vessel));
        values.add(getString(R.string.login_type_shore_control));
        if (preferences.isDemoAllowed()) {
            values.add(getString(R.string.login_type_viewer));
        }

        final List<CheckedItem> items = new ArrayList<>();
        for (String displayedText : values) {
            CheckedItem item = new CheckedItem();
            item.setText(displayedText);
            items.add(item);
        }

        final CheckedItemAdapter adapter = new CheckedItemAdapter(getActivity().getBaseContext(), items);
        setListAdapter(adapter);

        host = (PositionSelectedListenerHost) getActivity();

        return parent;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter instanceof CheckedItemAdapter) {
            CheckedItemAdapter checkedItemAdapter = (CheckedItemAdapter) adapter;
            checkedItemAdapter.setCheckedPosition(position);
            checkedItemAdapter.notifyDataSetChanged();
        }
        LoginType selectedLoginType;
        switch (position) {
            // see loginTypeDescriptions for the indices of the login types
            case 0:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_OFFICER_START, 0);
                break;
            case 1:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_OFFICER_FINISH, 1);
                break;
            case 2:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_SHORE_CONTROL, 2);
                break;
            case 3:
                selectedLoginType = LoginType.VIEWER;
                preferences.setSendingActive(false);
                author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_VIEWER, 3);
                break;
            default:
                selectedLoginType = LoginType.NONE;
                break;
        }
        preferences.setAuthor(author);
        ExLog.i(getActivity(), PositionListFragment.class.getName(), "Logging in as: " + selectedLoginType + "->" + author);
        if (host != null) {
            host.onPositionSelected(selectedLoginType);
        }
    }

    public LogEventAuthorImpl getAuthor() {
        return author;
    }
}
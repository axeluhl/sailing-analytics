package com.sap.sailing.racecommittee.app.ui.fragments.lists;

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

import java.util.ArrayList;
import java.util.List;

public class PositionListFragment extends LoggableListFragment {

    private static final int PRIORITY_OFFICER_VESSEL = 1;
    private static final int PRIORITY_SHORE_CONTROL = 2;
    private static final int PRIORITY_VIEWER = 3;

    private LogEventAuthorImpl author;
    private AppPreferences preferences;
    private final List<LoginTypeItem> values = new ArrayList<>();

    private PositionSelectedListenerHost host;

    public static PositionListFragment newInstance(int priority) {
        final PositionListFragment positionListFragment = new PositionListFragment();
        final Bundle args = new Bundle();
        args.putInt(AppConstants.EXTRA_PRIORITY, priority);
        positionListFragment.setArguments(args);
        return positionListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        preferences = AppPreferences.on(getActivity());

        values.clear();
        values.add(new LoginTypeItem(PRIORITY_OFFICER_VESSEL, getString(R.string.login_type_officer_on_vessel),
                AppConstants.AUTHOR_TYPE_OFFICER_VESSEL, LoginType.OFFICER));
        values.add(new LoginTypeItem(PRIORITY_SHORE_CONTROL, getString(R.string.login_type_shore_control),
                AppConstants.AUTHOR_TYPE_SHORE_CONTROL, LoginType.OFFICER));
        // TODO define configuration to activate super user
        // values.add(new LoginTypeItem(0, getString(R.string.login_type_superuser), AppConstants.AUTHOR_TYPE_SUPERUSER,
        // LoginType.OFFICER));
        if (preferences.isDemoAllowed()) {
            values.add(new LoginTypeItem(PRIORITY_VIEWER, getString(R.string.login_type_viewer), AppConstants.AUTHOR_TYPE_VIEWER,
                    LoginType.VIEWER));
        }

        final List<CheckedItem> items = new ArrayList<>();
        for (LoginTypeItem loginType : values) {
            CheckedItem item = new CheckedItem();
            item.setText(loginType.label);
            items.add(item);
        }

        final CheckedItemAdapter adapter = new CheckedItemAdapter(getActivity(), items);
        setListAdapter(adapter);

        host = (PositionSelectedListenerHost) getActivity();

        final Bundle args = getArguments();
        if (args != null) {
            final int priority = args.getInt(AppConstants.EXTRA_PRIORITY);
            for (int i = 0; i < values.size(); i++) {
                final LoginTypeItem item = values.get(i);
                if (item.priority == priority) {
                    positionSelected(i, adapter);
                    break;
                }
            }
        }
        return view;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        ListAdapter adapter = listView.getAdapter();
        positionSelected(position, adapter);
    }

    private void positionSelected(int position, ListAdapter adapter) {
        if (adapter instanceof CheckedItemAdapter) {
            CheckedItemAdapter checkedItemAdapter = (CheckedItemAdapter) adapter;
            checkedItemAdapter.setCheckedPosition(position);
            checkedItemAdapter.notifyDataSetChanged();
        }
        LoginTypeItem item = values.get(position);
        LoginType selectedLoginType = item.type;
        author = new LogEventAuthorImpl(item.name, item.priority);
        preferences.setSendingActive(item.type == LoginType.OFFICER);
        preferences.setAuthor(author);
        ExLog.i(getActivity(), PositionListFragment.class.getName(),
                "Logging in as: " + selectedLoginType + "->" + author);
        if (host != null) {
            host.onPositionSelected(selectedLoginType);
        }
    }

    public LogEventAuthorImpl getAuthor() {
        return author;
    }

    private static class LoginTypeItem {

        private final int priority;
        private final String label;
        private final String name;
        private final LoginType type;

        LoginTypeItem(int priority, String label, String name, LoginType type) {
            this.priority = priority;
            this.label = label;
            this.name = name;
            this.type = type;
        }
    }
}
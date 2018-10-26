package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.LoginType;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItem;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PositionListFragment extends LoggableListFragment {

    private LogEventAuthorImpl author;
    private AppPreferences preferences;
    private List<LoginTypeItem> values;

    private PositionSelectedListenerHost host;

    public PositionListFragment() {
        values = new ArrayList<>();
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

        values.add(new LoginTypeItem(1, getString(R.string.login_type_officer_on_vessel),
                AppConstants.AUTHOR_TYPE_OFFICER_VESSEL, LoginType.OFFICER));
        values.add(new LoginTypeItem(2, getString(R.string.login_type_shore_control),
                AppConstants.AUTHOR_TYPE_SHORE_CONTROL, LoginType.OFFICER));
        // TODO define configuration to activate super user
        // values.add(new LoginTypeItem(0, getString(R.string.login_type_superuser), AppConstants.AUTHOR_TYPE_SUPERUSER,
        // LoginType.OFFICER));
        if (preferences.isDemoAllowed()) {
            values.add(new LoginTypeItem(3, getString(R.string.login_type_viewer), AppConstants.AUTHOR_TYPE_VIEWER,
                    LoginType.VIEWER));
        }

        final List<CheckedItem> items = new ArrayList<>();
        for (LoginTypeItem loginType : values) {
            CheckedItem item = new CheckedItem();
            item.setText(loginType.mLabel);
            items.add(item);
        }

        final CheckedItemAdapter adapter = new CheckedItemAdapter(getActivity(), items);
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
        LoginTypeItem item = values.get(position);
        LoginType selectedLoginType = item.mType;
        author = new LogEventAuthorImpl(item.mName, item.mPrio);
        preferences.setSendingActive(item.mType == LoginType.OFFICER);
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

        private int mPrio;
        private String mLabel;
        private String mName;
        private LoginType mType;

        LoginTypeItem(int prio, String label, String name, LoginType type) {
            mPrio = prio;
            mLabel = label;
            mName = name;
            mType = type;
        }
    }

}
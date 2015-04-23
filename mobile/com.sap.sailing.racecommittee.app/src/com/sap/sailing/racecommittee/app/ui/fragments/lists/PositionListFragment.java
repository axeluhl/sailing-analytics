package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog.LoginType;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

import java.util.ArrayList;

public class PositionListFragment extends LoggableListFragment {

    private LogEventAuthorImpl author;
    private AppPreferences preferences;
    private View lastSelected;

    private PositionSelectedListenerHost host;

    public PositionListFragment() {
    }

    public static PositionListFragment newInstance() {
        return new PositionListFragment();
    }

    @Override
    public void onResume() {
        final ArrayList<String> values = new ArrayList<>();
        values.add(getString(R.string.login_type_officer_on_start_vessel));
        values.add(getString(R.string.login_type_officer_on_finish_vessel));
        values.add(getString(R.string.login_type_shore_control));
        values.add(getString(R.string.login_type_viewer));

        this.preferences = AppPreferences.on(getActivity().getApplicationContext());

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getBaseContext(),
                R.layout.login_list_item, R.id.list_item, values);
        setListAdapter(adapter);
        getListView().setDivider(null);

        host = (PositionSelectedListenerHost) getActivity();

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.list_fragment, container, false);
        if (view != null) {
            parent.addView(view, 1);
        }
        return parent;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        setStyleClicked(view);

        LoginType selectedLoginType;
        switch (position) {
            // see loginTypeDescriptions for the indices of the login types
            case 0:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl("Race Officer on Start Vessel", 0);
                break;
            case 1:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl("Race Officer on Finish Vessel", 1);
                break;
            case 2:
                selectedLoginType = LoginType.OFFICER;
                preferences.setSendingActive(true);
                author = new LogEventAuthorImpl("Shore Control", 2);
                break;
            case 3:
                selectedLoginType = LoginType.VIEWER;
                preferences.setSendingActive(false);
                author = new LogEventAuthorImpl("Viewer", 3);
                break;
            default:
                selectedLoginType = LoginType.NONE;
                break;
        }
        preferences.setAuthor(author);
        if (host != null) {
            host.onPositionSelected(selectedLoginType);
        }
    }

    public LogEventAuthorImpl getAuthor() {
        return author;
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
}
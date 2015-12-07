package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.shared.util.EulaHelper;
import com.sap.sailing.android.shared.util.LicenseHelper;
import com.sap.sailing.android.tracking.app.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.model.Notices;

public class AboutFragment extends com.sap.sailing.android.ui.fragments.BaseFragment {

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        view.findViewById(R.id.licence_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicenceDialog();
            }
        });
        view.findViewById(R.id.eula_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EulaHelper.openEulaPage(getActivity());
            }
        });
        return view;
    }

    private void showLicenceDialog() {
        Notices notices = new Notices();
        LicenseHelper licenseHelper = new LicenseHelper();
        notices.addNotice(licenseHelper.getAndroidSupportNotice());
        notices.addNotice(licenseHelper.getOpenSansNotice());
        notices.addNotice(licenseHelper.getJsonSimpleNotice());
        notices.addNotice(licenseHelper.getDialogNotice());
        LicensesDialog.Builder builder = new LicensesDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.licence_information));
        builder.setNotices(notices);
        builder.build().show();
    }
}
package com.sap.sailing.android.shared.ui.activities;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.data.BaseCheckinData;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.customviews.OpenSansToolbar;
import com.sap.sailing.android.shared.ui.fragments.AbstractHomeFragment;
import com.sap.sailing.android.shared.util.EulaHelper;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import static io.branch.referral.Defines.Jsonkey.Clicked_Branch_Link;

public abstract class AbstractStartActivity<C extends BaseCheckinData> extends CheckinDataActivity<C> {

    private final static String TAG = AbstractStartActivity.class.getName();
    protected OpenSansToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        toolbar = (OpenSansToolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.hideSubtitle();
            toolbar.setTitleSize(20);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64dp);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        EulaHelper.with(this).showEulaDialogIfNotAccepted(new EulaHelper.OnEulaAcceptedListener() {
            @Override
            public void eulaAccepted() {
                Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
                    @Override
                    public void onInitFinished(JSONObject referringParams, BranchError error) {
                        if (error == null) {
                            try {
                                Boolean clickedBranchLink = referringParams.getBoolean(Clicked_Branch_Link.getKey());
                                if (!clickedBranchLink) {
                                    AbstractStartActivity.this.handleLegacyStart();
                                    return;
                                }
                                ExLog.i(AbstractStartActivity.this, "BRANCH SDK", referringParams.toString());
                                String checkinUrl = referringParams.getString(DeviceMappingConstants.URL_CHECKIN_URL);
                                if (checkinUrl != null) {
                                    ExLog.i(AbstractStartActivity.this, TAG, "handling branch.io deeplink.");
                                    getHomeFragment().handleScannedOrUrlMatchedUri(Uri.parse(checkinUrl));
                                    // if we don't clear the intent data here the next onStart cycle
                                    // will error in the legacy start procedure while trying to interpret
                                    // the branch.io deeplink as legacy link
                                    AbstractStartActivity.this.getIntent().setData(null);
                                }
                            } catch (JSONException e) {
                                ExLog.ex(AbstractStartActivity.this, TAG, e);
                            }
                        } else {
                            ExLog.i(AbstractStartActivity.this, "BRANCH SDK", error.getMessage());
                        }
                    }
                }, AbstractStartActivity.this.getIntent().getData(), AbstractStartActivity.this);
            }
        });
    }

    private void handleLegacyStart() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            ExLog.i(this, TAG, "Matched URL, handling scanned or matched URL.");
            getHomeFragment().handleScannedOrUrlMatchedUri(uri);
        }
        intent.setData(null);
    }

    public abstract AbstractHomeFragment getHomeFragment();

    @Override
    protected void onResume() {
        super.onResume();
        // checkForUpdates();

        int googleServicesResultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googleServicesResultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, googleServicesResultCode, 0);
            dialog.show();
        }
    }
}

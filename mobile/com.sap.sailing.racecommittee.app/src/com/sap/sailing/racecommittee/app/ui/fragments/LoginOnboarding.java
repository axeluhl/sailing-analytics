package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.QRHelper;

public class LoginOnboarding extends Fragment {

    private static final int requestCodeQR = 45392;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.login_onboarding, container, false);

        TextView link = ViewHelper.get(layout, R.id.get_started);
        if (link != null) {
            SpannableString string = new SpannableString(link.getText());
            string.setSpan(new UnderlineSpan(), 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            link.setText(string);
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.get_started_url)));
                    startActivity(intent);
                }
            });
        }

        Button scan = ViewHelper.get(layout, R.id.scanQr);
        if (scan != null) {
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                        startActivityForResult(intent, requestCodeQR);
                    } catch (Exception e) {
                        Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        startActivity(marketIntent);
                    }
                }
            });
        }

        Button manual = ViewHelper.get(layout, R.id.manual_input);
        if (manual != null) {
            manual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View view = View.inflate(v.getContext(), R.layout.login_onboarding_edit, null);
                    final EditText url = (EditText) view.findViewById(R.id.url);
                    final EditText device_id = (EditText) view.findViewById(R.id.device_id);
                    device_id.setText(AppPreferences.on(v.getContext()).getDeviceIdentifier());

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.AppTheme_AlertDialog);
                    builder.setView(view);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (QRHelper.with(getActivity()).saveData(url.getText().toString() + "#" + DeviceConfigurationQRCodeUtils.fragmentKey + "=" + device_id.getText().toString())) {
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConstants.INTENT_ACTION_RESET));
                            }
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    AlertDialog dialog = builder.show();

                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != requestCodeQR) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        switch (resultCode) {
            case Activity.RESULT_CANCELED:
                break;

            case Activity.RESULT_OK:
                QRHelper.with(getActivity()).saveData(data.getStringExtra("SCAN_RESULT"));
                break;

            default:
                Toast.makeText(getActivity(), getString(R.string.error_scanning_qr, resultCode), Toast.LENGTH_LONG).show();
        }
    }
}

package com.sap.sailing.racecommittee.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.sap.sailing.android.shared.data.http.UnauthorizedException;
import com.sap.sailing.android.shared.ui.activities.BarcodeCaptureActivity;
import com.sap.sailing.android.shared.util.AuthCheckTask;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.LoginTask.LoginTaskListener;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.BackPressListener;
import com.sap.sailing.racecommittee.app.services.BoardingService;
import com.sap.sailing.racecommittee.app.ui.activities.BaseActivity;
import com.sap.sailing.racecommittee.app.ui.activities.PreferenceActivity;
import com.sap.sailing.racecommittee.app.ui.activities.SystemInformationActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.GeneralPreferenceFragment;
import com.sap.sailing.racecommittee.app.utils.QRHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class LoginBackdrop extends Fragment implements BackPressListener {

    private static final String TAG = LoginBackdrop.class.getName();
    private static final int requestCodeQR = 45392;
    private static final String SHOW_BACKDROP_TEXT = "SHOW_BACKDROP_TEXT";

    private View login;
    private View onboarding;
    private boolean useBack;
    private String server;

    private LoginTaskListener loginTaskListener;
    private String username;
    private String password;

    public static LoginBackdrop newInstance() {

        Bundle args = new Bundle();
        args.putBoolean(SHOW_BACKDROP_TEXT, false);
        LoginBackdrop fragment = new LoginBackdrop();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.login_backdrop, container, false);

        ImageView settings = ViewHelper.get(layout, R.id.settings_button);
        if (settings != null) {
            settings.setOnClickListener(v -> openSettings());
        }

        ImageView info = ViewHelper.get(layout, R.id.technical_info);
        if (info != null) {
            info.setOnClickListener(v -> openInfo());
        }

        ImageView refresh = ViewHelper.get(layout, R.id.refresh_data);
        if (refresh != null) {
            refresh.setOnClickListener(v -> refreshData());
        }

        ImageView more = ViewHelper.get(layout, R.id.more);
        if (more != null) {
            more.setOnClickListener(new OverFlowButton());
        }

        setupOnboarding(layout);
        setupLogin(layout);


        loginTaskListener = new LoginTaskListener() {
            @Override
            public void onResultReceived(String accessToken) {
                if (LoginBackdrop.this.login != null) {
                    LoginBackdrop.this.login.setVisibility(View.GONE);
                }
                if (isAdded()) {
                    AppPreferences.on(getActivity()).setAccessToken(accessToken);
                    BroadcastManager.getInstance(getActivity())
                            .addIntent(new Intent(AppConstants.INTENT_ACTION_VALID_DATA));
                }
            }

            @Override
            public void onException(Exception exception) {
                LoginBackdrop.this.onException(exception);
            }
        };
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) context;
            baseActivity.setBackPressListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) getActivity();
            baseActivity.setBackPressListener(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null && getView() != null && !getArguments().getBoolean(SHOW_BACKDROP_TEXT, true)) {
            View view = getView().findViewById(R.id.backdrop_title);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void refreshData() {
        Intent intent = new Intent(AppConstants.INTENT_ACTION_RESET);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }

    private void openInfo() {
        Intent intent = new Intent(getActivity(), SystemInformationActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(getActivity(), PreferenceActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
        startActivity(intent);
    }

    private void requestQRCodeScan() {
        Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, requestCodeQR);
    }

    private void setupOnboarding(View layout) {
        onboarding = ViewHelper.get(layout, R.id.login_onboarding);

        TextView link = ViewHelper.get(layout, R.id.get_started);
        if (link != null) {
            formatText(link);
            link.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.get_started_url)));
                startActivity(intent);
            });
        }

        Button scan = ViewHelper.get(layout, R.id.scanQr);
        if (scan != null) {
            scan.setOnClickListener(v -> requestQRCodeScan());
        }

        Button manual = ViewHelper.get(layout, R.id.manual_input);
        if (manual != null) {
            manual.setOnClickListener(v -> {
                AppPreferences pref = AppPreferences.on(v.getContext());
                View view = View.inflate(v.getContext(), R.layout.login_onboarding_edit, null);
                final EditText url = view.findViewById(R.id.url);
                if (TextUtils.isEmpty(pref.getServerBaseURL())) {
                    url.setText(getString(R.string.preference_server_url_default));
                } else {
                    url.setText(pref.getServerBaseURL());
                }
                final EditText device_id = view.findViewById(R.id.device_id);
                device_id.setText(pref.getDeviceConfigurationName(null));

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    BoardingService.get().saveBackendUrl(requireContext(), url.getText().toString());
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.show();

                final Window window = dialog.getWindow();
                if (window != null) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });
        }
    }

    private void setupLogin(View layout) {
        login = ViewHelper.get(layout, R.id.login_form);

        View change = ViewHelper.get(layout, R.id.change_server);
        if (change != null) {
            change.setOnClickListener(v -> {
                useBack = true;
                BroadcastManager.getInstance(getActivity())
                        .addIntent(new Intent(AppConstants.INTENT_ACTION_SHOW_ONBOARDING));
            });
        }

        TextView url = ViewHelper.get(layout, R.id.server_url);
        if (url != null) {
            if ("\"SAP\"".equals(server)) {
                server = null;
            }
            if (TextUtils.isEmpty(server)) {
                server = AppPreferences.on(getActivity()).getServerBaseURL();
            }
            if (TextUtils.isEmpty(server)) {
                server = getString(R.string.not_available);
            }
            url.setText(server.replace("\"", ""));
        }

        final EditText userName = ViewHelper.get(layout, R.id.user_name);
        if (!TextUtils.isEmpty(username)){
            userName.setText(username);
        }
        final EditText userPassword = ViewHelper.get(layout, R.id.user_password);
        if (!TextUtils.isEmpty(password)){
            userPassword.setText(password);
        }
        Button login = ViewHelper.get(layout, R.id.login_request);
        if (login != null) {
            login.setOnClickListener(v -> {
                BoardingService.get().login(requireActivity(), userName.getText().toString(), userPassword.getText().toString(), loginTaskListener);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != requestCodeQR) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == CommonStatusCodes.SUCCESS && data != null) {
            Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
            QRHelper.with(getActivity()).saveData(barcode.displayValue);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_scanning_qr, resultCode), Toast.LENGTH_LONG).show();
        }
    }

    private void formatText(TextView textView) {
        if (textView != null) {
            SpannableString string = new SpannableString(textView.getText());
            string.setSpan(new UnderlineSpan(), 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            string.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.constant_sap_blue_1)),
                    0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(string);
        }
    }

    public void onException(Exception exception) {
        if (login != null) {
            if (login.getVisibility() == View.VISIBLE) { // login call
                if (exception instanceof UnauthorizedException) {
                    Toast.makeText(getActivity(), R.string.wrong_credentials, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.unexpected_error, Toast.LENGTH_LONG).show();
                }
            } else { // hello call
                server = null;
                if (exception instanceof UnauthorizedException) { // wrong credentials (access token)
                    server = exception.getMessage().split("=")[1];
                    showLoginView();
                } else { // connection error
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle(R.string.hello_call_error_title);
                    builder.setMessage(R.string.hello_call_error_message);
                    builder.setPositiveButton(R.string.hello_call_error_positive, (dialog, which) ->
                            checkLogin());
                    builder.setNegativeButton(R.string.hello_call_error_negative, (dialog, which) ->
                            requireActivity().finish());
                    builder.setNeutralButton(R.string.hello_call_error_neutral, (dialog, which) ->
                            showOnboarding());
                    builder.setCancelable(false);
                    builder.show();
                }
            }
        }
    }

    public void checkLogin() {
        if (!BoardingService.get().checkLogin(requireContext(), new AuthCheckTask.AuthCheckTaskListener() {
            @Override
            public void onResultReceived(Boolean authenticated) {
                if (authenticated) {
                    BroadcastManager.getInstance(getActivity())
                            .addIntent(new Intent(AppConstants.INTENT_ACTION_VALID_DATA));
                } else {
                    Toast.makeText(getActivity(), "User is not authenticated", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onException(Exception exception) {
                showOnboarding();
                LoginBackdrop.this.onException(exception);
            }
        })){
            showOnboarding();
        }
    }

    @Override
    public boolean handleBackPress() {
        if (useBack) {
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_SHOW_LOGIN));
            useBack = false;
            return true;
        } else {
            return false;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private class OverFlowButton implements View.OnClickListener {

        // Because of massive usage of reflection (try {} catch ())
        // Don't know how to fix the warning a better way
        @Override
        public void onClick(View view) {
            if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1) {
                PopupMenu popupMenu;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    popupMenu = new PopupMenu(getActivity(), view, Gravity.RIGHT);
                } else {
                    popupMenu = new PopupMenu(getActivity(), view);
                }
                popupMenu.inflate(R.menu.login_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.technical_info:
                            openInfo();
                            break;

                        case R.id.settings_button:
                            openSettings();
                            break;

                        default:
                            refreshData();
                    }
                    return true;
                });
                popupMenu.show();
                ThemeHelper.positioningPopupMenu(getActivity(), popupMenu, view);
            }
        }
    }

    private void showOnboarding() {
        hideAll();
        setVisibility(onboarding, View.VISIBLE);
    }

    private void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    void hideAll() {
        setVisibility(onboarding, View.GONE);
        setVisibility(login, View.GONE);
    }
    private void showLoginView() {
        hideAll();
        setupLogin(getView());
        setVisibility(login, View.VISIBLE);
    }
}

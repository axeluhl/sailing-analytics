package com.sap.sailing.racecommittee.app.utils.autoupdate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class AutoUpdaterChecker {

    public static final String TAG = AutoUpdaterChecker.class.getName();

    public interface AutoUpdaterState {
        void onApkDownloadProgress(Float progress);

        void updateToVersion(int version, String apkFileName);

        void onApkDownloadFinished(File result);

        void onError();
    }

    private final Context context;
    private final ProgressDialog dialog;
    private final AutoUpdater updater;
    private final AutoUpdaterStateImpl state;

    public AutoUpdaterChecker(Context context, AutoUpdater updater, boolean forceUpdate) {
        this.context = context;
        this.updater = updater;
        this.state = new AutoUpdaterStateImpl(forceUpdate);

        this.dialog = new ProgressDialog(context);
        dialog.setTitle("Auto-Update");
        dialog.setCancelable(true);
        dialog.setButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.cancel();
            }
        });
    }

    public void check(URL serverUrl) {
        state.start(serverUrl);
    }

    private class AutoUpdaterStateImpl implements AutoUpdaterState {

        private final boolean forceUpdate;
        private URL serverUrl;
        
        public AutoUpdaterStateImpl(boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
        }

        public void start(URL serverUrl) {
            this.serverUrl = serverUrl;

            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setIndeterminate(true);
            dialog.setMessage("Checking for version information on server...");
            dialog.show();
            
            try {
                URL versionUrl = composeVersionUrl();
                ExLog.i(TAG, String.format("Trying to download auto-update info from %s", versionUrl.toString()));

                final AutoUpdaterVersionDownloader downloader = new AutoUpdaterVersionDownloader(this);
                downloader.execute(versionUrl);
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloader.cancel(true);
                    }
                });
            } catch (MalformedURLException e) {
                onError();
                Toast.makeText(context, "The version file link was not valid.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void updateToVersion(int serverVersion, final String apkFileName) {
            int currentVersion = RaceApplication.getPackageInfo(context).versionCode;
            boolean needsUpdate = currentVersion != serverVersion;
            ExLog.i(TAG, String.format("Server version is %d. Local version is %d.", serverVersion, currentVersion));
            
            DialogInterface.OnClickListener dismissListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface prompt, int which) {
                    dialog.cancel();
                    prompt.dismiss();
                }
            };
            DialogInterface.OnCancelListener cancelListener = new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface prompt) {
                    dialog.cancel();
                }
            };
            DialogInterface.OnClickListener updateListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface prompt, int which) {
                    downloadUpdate(apkFileName);
                    prompt.dismiss();
                }
            };

            String messageFormat = needsUpdate ? "Click 'Install' to perform the update to version %d." : "You already have installed the version offered by the server (version %d).";
            
            AlertDialog.Builder updateDialog = new AlertDialog.Builder(context);
            updateDialog
                .setTitle("Auto-Update")
                .setMessage(String.format(messageFormat, serverVersion))
                .setPositiveButton(context.getString(needsUpdate ? R.string.auto_update_install : android.R.string.ok), needsUpdate ? updateListener : dismissListener)
                .setNegativeButton(context.getString(needsUpdate ?  android.R.string.cancel : R.string.auto_update_install_anyway), needsUpdate ? dismissListener : updateListener)
                .setOnCancelListener(cancelListener);
            if (needsUpdate || forceUpdate) {
                updateDialog.show();
            } else {
                dialog.dismiss();
                Toast.makeText(context, "You are already up to date!", Toast.LENGTH_LONG).show();
            }
        }

        private void downloadUpdate(String apkFileName) {
            dialog.setMessage("Downloading APK from server...");
            
            try {
                File target = updater.createApkTargetFile();
                
                final AutoUpdaterApkDownloader downloader = new AutoUpdaterApkDownloader(this, target);
                
                URL downloadUrl = composeDownloadUrl(apkFileName);
                ExLog.i(TAG, String.format("Download from %s to file %s.", downloadUrl.toString() , target.getAbsolutePath()));
                downloader.execute(downloadUrl);
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloader.cancel(true);
                    }
                });
            } catch (MalformedURLException e) {
                onError();
                Toast.makeText(context, "The download link was not valid.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                onError();
                Toast.makeText(context, "The download file couldn't be created.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onApkDownloadProgress(Float progress) {
            if (dialog.isIndeterminate()) {
                dialog.setIndeterminate(false);
            }
            dialog.setProgress((int) (progress * 100));
        }

        @Override
        public void onApkDownloadFinished(File result) {
            dialog.dismiss();
            updater.updateFromFile(result);
        }

        @Override
        public void onError() {
            dialog.dismiss();
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
            errorDialog
                .setTitle("Auto-Update")
                .setMessage("Error while trying to auto-update.")
                .setIcon(context.getResources().getDrawable(android.R.drawable.ic_dialog_alert))
                .setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                .show();
        }
        
        protected URL composeVersionUrl() throws MalformedURLException {
            String packageName = context.getPackageName();
            return new URL(serverUrl.getProtocol(), serverUrl.getHost(), serverUrl.getPort(), serverUrl.getPath()
                    + "/apps/" + packageName + ".version");
        }

        protected URL composeDownloadUrl(String apkFileName) throws MalformedURLException {
            return new URL(serverUrl.getProtocol(), serverUrl.getHost(), serverUrl.getPort(), serverUrl.getPath()
                    + "/apps/" + apkFileName);
        }
    };

}

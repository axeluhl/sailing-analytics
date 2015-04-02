package com.sap.sailing.android.tracking.app.ui.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.AbstractRegattaActivity;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.CheckinManager;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;

public class RegattaActivity extends AbstractRegattaActivity implements RegattaFragment.FragmentWatcher {

    private final static String TAG = RegattaActivity.class.getName();
    private final static String LEADERBOARD_IMAGE_FILENAME_PREFIX = "leaderboardImage_";
    private final static String FLAG_IMAGE_FILENAME_PREFIX = "flagImage_";

    public EventInfo event;
    public CompetitorInfo competitor;
    public LeaderboardInfo leaderboard;
    public CheckinUrlInfo checkinUrl;

    private boolean hasPicture;
    private String checkinDigest;
    private CheckinManager manager;
    private AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
        Intent intent = getIntent();

        event = new EventInfo();
        competitor = new CompetitorInfo();
        leaderboard = new LeaderboardInfo();

        checkinDigest = intent.getStringExtra(getString(R.string.checkin_digest));

        checkinUrl = DatabaseHelper.getInstance().getCheckinUrl(this, checkinDigest);
        manager = new CheckinManager(checkinUrl.urlString, this);

        competitor = DatabaseHelper.getInstance().getCompetitor(this, checkinDigest);
        event = DatabaseHelper.getInstance().getEventInfo(this, checkinDigest);
        leaderboard = DatabaseHelper.getInstance().getLeaderboard(this, checkinDigest);

        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            getSupportActionBar().setTitle(leaderboard.name);
            getSupportActionBar().setSubtitle(event.name);
        }
        RegattaFragment regattaFragment = new RegattaFragment();
        regattaFragment.setFragmentWatcher(this);
        replaceFragment(R.id.content_frame, regattaFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (prefs.getTrackerIsTracking()) {
            String checkinDigest = prefs.getTrackerIsTrackingCheckinDigest();
            startTrackingActivity(checkinDigest);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_with_checkout, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem teamPhotoItem = menu.findItem(R.id.options_menu_add_team_image);
        if (hasPicture) {
            teamPhotoItem.setTitle(getString(R.string.options_replace_team_photo));
        } else {
            teamPhotoItem.setTitle(getString(R.string.options_add_team_photo));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_menu_settings:
                ExLog.i(this, TAG, "Clicked SETTINGS.");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.options_menu_checkout:
                ExLog.i(this, TAG, "Clicked CHECKOUT.");
                checkout();
                return true;
            case R.id.options_menu_add_team_image:
                ExLog.i(this, TAG, "Clicked ADD TEAM IMAGE");
                getRegattaFragment().showChooseExistingPictureOrTakeNewPhotoAlert();
                return true;
            case R.id.options_menu_refresh:
                manager.callServerAndGenerateCheckinData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private RegattaFragment getRegattaFragment() {
        return (RegattaFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    private void userImageUpdated() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hasPicture = true;
                LinearLayout addTeamPhotoTextView = (LinearLayout) findViewById(R.id.add_photo);
                addTeamPhotoTextView.setVisibility(View.INVISIBLE);

                getRegattaFragment().setChangePhotoButtonHidden(false);
            }
        });
    }

    // TODO What's this code? Is it still needed?
    // private void flagImageUpdated() {
    // runOnUiThread(new Runnable() {
    // @Override
    // public void run() {
    // ImageView flagImageView = (ImageView) findViewById(R.id.flag_image);
    // }
    // });
    // }

    @Override
    protected void onResume() {
        setUpView();
        RegattaFragment regattaFragment = getRegattaFragment();
        if (regattaFragment != null) {
            regattaFragment.setFragmentWatcher(this);
        }
        super.onResume();
    }

    private void setUpView() {
        TextView competitorNameTextView = (TextView) findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitor.name);

        TextView sailIdTextView = (TextView) findViewById(R.id.sail_id);
        sailIdTextView.setText(competitor.sailId);

        // TODO What's this code? Is it still needed?
        // ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        // String flagStr = String.format("%s.png", countryCode);
        // String uri = "@drawable/" + competitor.countryCode.toLowerCase(Locale.getDefault());
        //
        // int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        //
        // try {
        // Drawable res = getResources().getDrawable(imageResource);
        // flagImageView.setImageDrawable(res);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        getRegattaFragment().setChangePhotoButtonHidden(true);
        // ExLog.w(this, TAG, "Event Image URL: " + event.imageUrl);

        ImageView imageView = (ImageView) findViewById(R.id.userImage);
        Bitmap storedImage = getStoredImage(getLeaderboardImageFileName(leaderboard.name));
        if (storedImage == null) {
            if (event.imageUrl != null) {
                new DownloadLeaderboardImageTask(imageView).execute(event.imageUrl);
            }
        } else {
            imageView.setImageBitmap(storedImage);
            userImageUpdated();
        }


        ImageView flagImageView = (ImageView) findViewById(R.id.flag_image);
        Bitmap storedFlagImage = getStoredImage(getFlagImageFileName(competitor.countryCode.toLowerCase(Locale
                .getDefault())));
        if (storedFlagImage == null) {
            String urlStr = String.format("%s/gwt/images/flags/%s.png", event.server,
                    competitor.countryCode.toLowerCase(Locale.getDefault()));
            new DownloadFlagImageTask(flagImageView, competitor.countryCode).execute(urlStr);
        } else {
            flagImageView.setImageBitmap(storedFlagImage);
        }
    }

    /**
     * @param bitmap
     */
    public void updateLeaderboardPictureChosenByUser(final Bitmap bitmap) {
        storeImageAndSendToServer(bitmap, getLeaderboardImageFileName(leaderboard.name), true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.userImage);
                imageView.setImageBitmap(bitmap);
            }
        });
        userImageUpdated();
    }

    /**
     * Store image for quicker retrieval later and trigger upload to server.
     *
     * @param images
     */
    private void storeImageAndSendToServer(Bitmap image, String fileName, boolean sendToServer) {
        File pictureFile = getImageFile(fileName);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            if (sendToServer) {
                sendTeamImageToServer(pictureFile);
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /**
     * Get stored image if there's one saved.
     *
     * @return
     */
    private Bitmap getStoredImage(String fileName) {
        File pictureFile = getImageFile(fileName);
        if (pictureFile == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap image = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
        return image;
    }

    /**
     * Silently send the team image to the server.
     *
     * @param imageFile
     */
    private void sendTeamImageToServer(File imageFile) {
        if (BuildConfig.DEBUG) {
            ExLog.i(this, TAG, "Sending imageFile to server: " + imageFile);
        }
        final String uploadURLStr = event.server
                + prefs.getServerUploadTeamImagePath().replace("{competitor_id}", competitor.id);
        new UploadTeamImageTask(imageFile).execute(uploadURLStr);
    }

    /**
     * Get Path for cached leaderbaord image.
     *
     * @return
     */
    public File getImageFile(String fileName) {

        File mediaStorageDir = getMediaStorageDir();

        File mediaFile;
        String mImageName = "MI_" + fileName + ".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void deleteImageFile(String fileName) {
        File mediaStorageDir = getMediaStorageDir();

        File mediaFile;
        String mImageName = "MI_" + fileName + ".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

        mediaFile.delete();
    }

    public File getMediaStorageDir() {
        File mediaStorageDir;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/"
                    + getApplicationContext().getPackageName() + "/Files");
        } else {
            mediaStorageDir = getApplicationContext().getCacheDir();
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        return mediaStorageDir;
    }

    @Override
    public void onCheckinDataAvailable(AbstractCheckinData checkinData) {
        if (checkinData != null) {
        	CheckinData data = (CheckinData) checkinData;
            try {
                DatabaseHelper.getInstance().deleteRegattaFromDatabase(this, checkinDigest);
                DatabaseHelper.getInstance().storeCheckinRow(this, data.getEvent(),
                        data.getCompetitor(), data.getLeaderboard(), data.getCheckinUrl());
                competitor = DatabaseHelper.getInstance().getCompetitor(this, checkinDigest);
                event = DatabaseHelper.getInstance().getEventInfo(this, checkinDigest);
                leaderboard = DatabaseHelper.getInstance().getLeaderboard(this, checkinDigest);
                checkinUrl = DatabaseHelper.getInstance().getCheckinUrl(this, checkinDigest);
                RegattaFragment regattaFragment = new RegattaFragment();
                regattaFragment.setFragmentWatcher(this);
                replaceFragment(R.id.content_frame, regattaFragment);
            } catch (DatabaseHelper.GeneralDatabaseHelperException e) {
                ExLog.e(this, TAG, "Batch insert failed: " + e.getMessage());
                displayDatabaseError();
            }
        }
    }

    @Override
    public void onViewCreated() {
        setUpView();
    }

    private class UploadTeamImageTask extends AsyncTask<String, Void, String> {
        File imageFile;
        String uploadUrl;

        public UploadTeamImageTask(File imageFile) {
            this.imageFile = imageFile;
        }

        protected String doInBackground(String... urls) {
            uploadUrl = urls[0];
            try {
                if (imageFile != null) {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(uploadUrl);
                    InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(imageFile), imageFile.length());
                    reqEntity.setContentType("image/jpeg");
                    reqEntity.setChunked(true); // Send in multiple parts if needed
                    httppost.setEntity(reqEntity);
                    httpclient.execute(httppost);
                }
            } catch (IOException e) {
                ExLog.e(RegattaActivity.this, TAG, "Error uploading image: " + e.getLocalizedMessage());
                this.cancel(true);
            }

            return "";
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(RegattaActivity.this, getString(R.string.error_sending_team_image_to_server),
                    Toast.LENGTH_LONG).show();
        }

        protected void onPostExecute(String result) {
            // do something
        }
    }

    private class DownloadLeaderboardImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String downloadUrl;

        public DownloadLeaderboardImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            downloadUrl = urls[0];
            Bitmap mIcon11 = null;

            try {
                InputStream in = new java.net.URL(downloadUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
                storeImageAndSendToServer(result, getLeaderboardImageFileName(leaderboard.name), false);
                userImageUpdated();
            } else {
                ExLog.e(RegattaActivity.this, TAG, "Failed to download leaderboard image at url " + downloadUrl);
            }
        }
    }

    private class DownloadFlagImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String countryCode;
        String downloadUrl;

        public DownloadFlagImageTask(ImageView bmImage, String countryCode) {
            this.bmImage = bmImage;
            this.countryCode = countryCode;
        }

        protected Bitmap doInBackground(String... urls) {
            downloadUrl = urls[0];
            Bitmap mIcon11 = null;

            System.out.println("URL DISPLAY: " + downloadUrl);

            try {
                InputStream in = new java.net.URL(downloadUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            System.out.println("RESULT: " + result);
            if (result != null) {
                bmImage.setImageBitmap(result);
                storeImageAndSendToServer(result, getFlagImageFileName(countryCode), false);
            } else {
                ExLog.e(RegattaActivity.this, TAG, "Failed to download flag image at url " + downloadUrl);
            }
        }
    }

    private String getLeaderboardImageFileName(String leaderboardName) {
        return LEADERBOARD_IMAGE_FILENAME_PREFIX + leaderboardName;
    }

    private String getFlagImageFileName(String countryCode) {
        return FLAG_IMAGE_FILENAME_PREFIX + countryCode;
    }

    /**
     * Check out from regatta;
     */
    public void checkout() {
        final String checkoutURLStr = event.server
                + prefs.getServerCheckoutPath().replace("{leaderboard-name}", Uri.encode(leaderboard.name));

        showProgressDialog(R.string.please_wait, R.string.checking_out);

        JSONObject checkoutData = new JSONObject();
        try {
            checkoutData.put("competitorId", competitor.id);
            checkoutData.put("deviceUuid", UniqueDeviceUuid.getUniqueId(this));
            checkoutData.put("toMillis", System.currentTimeMillis());
        } catch (JSONException e) {
            showErrorPopup(R.string.error, R.string.error_could_not_complete_operation_on_server_try_again);
            ExLog.e(this, TAG, "Error populating checkout-data: " + e.getMessage());
            return;
        }

        try {
            HttpJsonPostRequest request = new HttpJsonPostRequest(new URL(checkoutURLStr), checkoutData.toString(),
                    this);
            NetworkHelper.getInstance(this).executeHttpJsonRequestAsnchronously(request,
                    new NetworkHelperSuccessListener() {

                        @Override
                        public void performAction(JSONObject response) {
                            DatabaseHelper.getInstance().deleteRegattaFromDatabase(RegattaActivity.this,
                                    event.checkinDigest);
                            deleteImageFile(getLeaderboardImageFileName(leaderboard.name));
                            dismissProgressDialog();
                            finish();
                        }
                    }, new NetworkHelperFailureListener() {

                        @Override
                        public void performAction(NetworkHelperError e) {
                            dismissProgressDialog();
                            showErrorPopup(R.string.error,
                                    R.string.error_could_not_complete_operation_on_server_try_again);
                        }
                    });

        } catch (MalformedURLException e) {
            ExLog.w(this, TAG, "Error, can't check out, MalformedURLException: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void startTrackingActivity(String checkinDigest) {
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra(getString(R.string.tracking_activity_checkin_digest_parameter), checkinDigest);
        startActivity(intent);
    }
    
    @Override
    protected int getOptionsMenuResId() {
        return R.menu.options_menu_with_checkout;
    }
}

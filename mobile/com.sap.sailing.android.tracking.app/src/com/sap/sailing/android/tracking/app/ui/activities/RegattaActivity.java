package com.sap.sailing.android.tracking.app.ui.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

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

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.tracking.app.utils.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.LeaderboardInfo;

public class RegattaActivity extends BaseActivity {

    private final static String TAG = RegattaActivity.class.getName();
    private final static String LEADERBOARD_IMAGE_FILENAME_PREFIX = "leaderboardImage_";
    private final static String FLAG_IMAGE_FILENAME_PREFIX = "flagImage_";
   
    public EventInfo event;
    public CompetitorInfo competitor;
    public LeaderboardInfo leaderboard;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        event = new EventInfo();
        competitor = new CompetitorInfo();
        leaderboard = new LeaderboardInfo();
        
        String checkinDigest = intent.getStringExtra(getString(R.string.checkin_digest));

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
        
        replaceFragment(R.id.content_frame, new RegattaFragment());	
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (prefs.getTrackerIsTracking())
        {
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
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    private RegattaFragment getRegattaFragment()
    {
    	return (RegattaFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

	private void userImageUpdated() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LinearLayout addTeamPhotoTextView = (LinearLayout) findViewById(R.id.add_photo);
				addTeamPhotoTextView.setVisibility(View.INVISIBLE);
				
				getRegattaFragment().setChangePhotoButtonHidden(false);
			}
		});
    }
	
//	private void flagImageUpdated() {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				ImageView flagImageView = (ImageView) findViewById(R.id.flag_image);				
//			}
//		});
//    }

    @Override
    protected void onResume() {
        TextView competitorNameTextView = (TextView)findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitor.name);
        
        TextView sailIdTextView = (TextView)findViewById(R.id.sail_id);
        sailIdTextView.setText(competitor.sailId);
        
//        ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        //String flagStr = String.format("%s.png", countryCode);
//        String uri = "@drawable/" + competitor.countryCode.toLowerCase(Locale.getDefault());
//        
//        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
//        
//		try {
//			Drawable res = getResources().getDrawable(imageResource);
//			flagImageView.setImageDrawable(res);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		getRegattaFragment().setChangePhotoButtonHidden(true);
		//ExLog.w(this, TAG, "Event Image URL: " + event.imageUrl);
		
		ImageView imageView = (ImageView) findViewById(R.id.userImage);
		Bitmap storedImage = getStoredImage(getLeaderboardImageFileName(leaderboard.name));
		
		if (storedImage == null)
		{
			if (event.imageUrl != null)
			{
				new DownloadLeaderboardImageTask(imageView).execute(event.imageUrl);
			}
		}
		else
		{
			imageView.setImageBitmap(storedImage);
			userImageUpdated();
		}
		
		ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
		Bitmap storedFlagImage = getStoredImage(getFlagImageFileName(competitor.countryCode.toLowerCase(Locale.getDefault())));
		
		if (storedFlagImage == null) {
			String urlStr = String.format("%s/gwt/images/flags/%s.png", event.server,
					competitor.countryCode.toLowerCase(Locale.getDefault()));
			new DownloadFlagImageTask(flagImageView, competitor.countryCode).execute(urlStr);
		} else {
			flagImageView.setImageBitmap(storedFlagImage);
		}
		
    	super.onResume();
    }
    
    /**
     * 
     * @param bitmap
     */
    public void updateLeaderboardPictureChosenByUser(final Bitmap bitmap)
    {
    	storeImage(bitmap, getLeaderboardImageFileName(leaderboard.name));
    	
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
     * Store image for quicker retrieval later.
     * @param images
     */
    private void storeImage(Bitmap image, String fileName) {
        File pictureFile = getImageFile(fileName);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        } 
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }  
    }
    
    /**
     * Get stored image if there's one saved.
     * @return
     */
    private Bitmap getStoredImage(String fileName)
    {
    	File pictureFile = getImageFile(fileName);
    	if (pictureFile == null)
    	{
    		return null;
    	}
    	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap image = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
		return image;
    }
    

//    private File getMediaFile(){
//    	
//    	
//    } 
    
    /**
     * Get Path for cached leaderbaord image.
     * @return
     */
    public File getImageFile(String fileName) {
    	
    	File mediaStorageDir = getMediaStorageDir();

        File mediaFile;
            String mImageName="MI_"+ fileName +".png";
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
			mediaStorageDir = new File(
					Environment.getExternalStorageDirectory()
							+ "/Android/data/"
							+ getApplicationContext().getPackageName()
							+ "/Files");
		} else {
			mediaStorageDir = getApplicationContext().getCacheDir();
		}

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        
        return mediaStorageDir;
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
				storeImage(result, getLeaderboardImageFileName(leaderboard.name));
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
				storeImage(result, getFlagImageFileName(countryCode));
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
	public void checkout()
	{
		final String checkoutURLStr = event.server
				+ prefs.getServerCheckoutPath().replace("{leaderboard-name}",
						Uri.encode(leaderboard.name));
		
		showProgressDialog(R.string.please_wait, R.string.checking_out);
		
		JSONObject checkoutData = new JSONObject();
		try {
			checkoutData.put("competitorId", competitor.id);
			checkoutData.put("deviceUuid", UniqueDeviceUuid.getUniqueId(this));
			checkoutData.put("toMillis", System.currentTimeMillis());
		} catch (JSONException e) {
			showErrorPopup(
					R.string.error,
					R.string.error_could_not_complete_operation_on_server_try_again);
			ExLog.e(this, TAG, "Error populating checkout-data: " + e.getMessage());
			return;
		}
		
		try {
			HttpJsonPostRequest request = new HttpJsonPostRequest(new URL(checkoutURLStr), checkoutData.toString(), this);
			NetworkHelper.getInstance(this).executeHttpJsonRequestAsnchronously(request, new NetworkHelperSuccessListener() {
				
				@Override
				public void performAction(JSONObject response) {
					DatabaseHelper.getInstance().deleteRegattaFromDatabase(RegattaActivity.this, event.checkinDigest);
					deleteImageFile(getLeaderboardImageFileName(leaderboard.name));
					dismissProgressDialog();
					finish();
				}
			}, new NetworkHelperFailureListener() {
				
				@Override
				public void performAction(NetworkHelperError e) {
					dismissProgressDialog();
					showErrorPopup(R.string.error,R.string.error_could_not_complete_operation_on_server_try_again);
				}
			});
			
		} catch (MalformedURLException e) {
			ExLog.w(this, TAG, "Error, can't check out, MalformedURLException: " + e.getMessage());
		}
	}
	
	@Override
	public void onBackPressed() {
		RegattaFragment fragment = (RegattaFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);

		if (fragment.isShowingBigCheckoutButton())
		{
			//do nothing, user must checkout himself at this point.
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	private void startTrackingActivity(String checkinDigest) {
		Intent intent = new Intent(this, TrackingActivity.class);
		intent.putExtra(getString(R.string.tracking_activity_checkin_digest_parameter), checkinDigest);
		startActivity(intent);
	}
}

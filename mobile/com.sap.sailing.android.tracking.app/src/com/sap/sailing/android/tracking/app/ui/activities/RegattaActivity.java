package com.sap.sailing.android.tracking.app.ui.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.internal.cm;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.LeaderboardInfo;

public class RegattaActivity extends BaseActivity {

    private final static String TAG = RegattaActivity.class.getName();
   
    private EventInfo event;
    private CompetitorInfo competitor;
    private LeaderboardInfo leaderboard;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        event = new EventInfo();
        competitor = new CompetitorInfo();
        leaderboard = new LeaderboardInfo();
        
        String leaderboardName = intent.getStringExtra(getString(R.string.leaderboard_name));
        String eventId = intent.getStringExtra(getString(R.string.event_id));
        String competitorId = intent.getStringExtra(getString(R.string.competitor_id));

        competitor = DatabaseHelper.getInstance(this).getCompetitor(competitorId);
		event = DatabaseHelper.getInstance(this).getEventInfo(eventId);
		leaderboard = DatabaseHelper.getInstance(this).getLeaderboard(leaderboardName);
        
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
            getSupportActionBar().setTitle(leaderboardName);
            getSupportActionBar().setSubtitle(event.name);
        }
        
        replaceFragment(R.id.content_frame, new RegattaFragment());	
        
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
//        case R.id.options_menu_info:
//            ExLog.i(this, TAG, "Clicked INFO.");
//            startActivity(new Intent(this, SystemInformationActivity.class));
//            return true;
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

    @Override
    protected void onResume() {
        TextView competitorNameTextView = (TextView)findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitor.name);
        
        TextView sailIdTextView = (TextView)findViewById(R.id.sail_id);
        sailIdTextView.setText(competitor.sailId);
        
        ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        //String flagStr = String.format("%s.png", countryCode);
        String uri = "@drawable/" + competitor.countryCode.toLowerCase(Locale.getDefault());
        
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        
		try {
			Drawable res = getResources().getDrawable(imageResource);
			flagImageView.setImageDrawable(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getRegattaFragment().setChangePhotoButtonHidden(true);
		
		if (event.imageUrl != null)
		{
			ImageView imageView = (ImageView) findViewById(R.id.userImage);
	        Bitmap storedImage = getStoredImage();
	        if (storedImage != null)
	        {
	        	imageView.setImageBitmap(storedImage);
	        	userImageUpdated();
	        }
	        else
	        {
	        	new DownloadImageTask(imageView).execute(event.imageUrl);
	        }
			
		}
		
    	super.onResume();
    }
    
    /**
     * 
     * @param bitmap
     */
    public void updatePictureChosenByUser(final Bitmap bitmap)
    {
    	storeImage(bitmap);
    	
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
    private void storeImage(Bitmap image) {
        File pictureFile = getMediaFile();
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
     * Get leaderboard image if there's one saved.
     * @return
     */
    private Bitmap getStoredImage()
    {
    	File pictureFile = getMediaFile();
    	if (pictureFile == null)
    	{
    		return null;
    	}
    	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap image = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(),
				options);
		return image;
    }
    
    /**
     * Get Path for cached leaderbaord image.
     * @return
     */
    private File getMediaFile(){
    	
		File mediaStorageDir;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
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

        File mediaFile;
            String mImageName="MI_"+ leaderboard.name +".png";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
        return mediaFile;
    } 
    
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
			storeImage(result);
			userImageUpdated();
		}
	}
	
	/**
	 * Check out from regatta;
	 */
	public void checkout()
	{
		final String checkoutURLStr = prefs.getServerURL()
				+ prefs.getServerCheckoutPath().replace("{leaderboard-name}",
						Uri.encode(leaderboard.name));
		
		showProgressDialog(R.string.please_wait, R.string.checking_out);
		
		JSONObject checkoutData = new JSONObject();
		try {
			checkoutData.put("competitorId", competitor.id);
			checkoutData.put("deviceUuid", UniqueDeviceUuid.getUniqueId(this));
			checkoutData.put("toMillis", String.valueOf(System.currentTimeMillis()));
		} catch (JSONException e) {
			showErrorPopup(
					R.string.error,
					R.string.error_could_not_complete_operation_on_server_try_again);
			ExLog.e(this, TAG, "Error populating checkout-data: " + e.getMessage());
			return;
		}
		
		ExLog.w(this, TAG, "CHECKOUT DATA: " + checkoutData);

		
		JsonObjectRequest checkoutRequest = new JsonObjectRequest(checkoutURLStr, checkoutData, new Listener<JSONObject>(){
					@Override
					public void onResponse(JSONObject response) {
						DatabaseHelper.getInstance(RegattaActivity.this).deleteRegttaFromDatabase(event, competitor, leaderboard);
						dismissProgressDialog();
						finish();
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						dismissProgressDialog();
						showErrorPopup(
								R.string.error,
								R.string.error_could_not_complete_operation_on_server_try_again);

					}
				});

		VolleyHelper.getInstance(this).addRequest(checkoutRequest);
	}
	
	public EventInfo getEvent()
	{
		return event;
	}
}

package com.sap.sailing.android.tracking.app.ui.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;

public class RegattaActivity extends BaseActivity {

    private final static String TAG = RegattaActivity.class.getName();
    
    private String eventId;
    private String competitorId;
    
    private String competitorName;
    private String competitorCountryCode;
    private String competitorSailId;
    
    private String eventName;
    private String eventImageUrl;
    
    private String leaderboardName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        leaderboardName = intent.getStringExtra(getString(R.string.leaderboard_name));
        eventId = intent.getStringExtra(getString(R.string.event_id));
        competitorId = intent.getStringExtra(getString(R.string.competitor_id));

        fetchData(eventId, competitorId);
        
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(leaderboardName);
            Spannable subtitle = new SpannableString("Registered for: " + eventName);
            StyleSpan styleBold = new StyleSpan(Typeface.BOLD);
            subtitle.setSpan(styleBold, 16, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setSubtitle(subtitle);
        }

        replaceFragment(R.id.content_frame, new RegattaFragment());
    }
    
    private RegattaFragment getRegattaFragment()
    {
    	return (RegattaFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }
    
    private void fetchData(String eventId, String competitorId)
    {
    	Cursor cc = getContentResolver().query(Competitor.CONTENT_URI, null, "competitor_id = \"" + competitorId + "\"", null, null);
		if (cc.moveToFirst()) {
			competitorName = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME));
			competitorCountryCode = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE));
			competitorSailId = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_SAIL_ID));
        }
		
		cc.close();
		
		Cursor ec = getContentResolver().query(Event.CONTENT_URI, null, "event_id = \"" + eventId + "\"", null, null);
		if (ec.moveToFirst()) {
			eventName = ec.getString(ec.getColumnIndex(Event.EVENT_NAME));
			eventImageUrl = ec.getString(ec.getColumnIndex(Event.EVENT_IMAGE_URL));
        }
		
		ec.close();
    }

	private void userImageUpdated() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LinearLayout addTeamPhotoTextView = (LinearLayout) findViewById(R.id.addFoto);
				addTeamPhotoTextView.setVisibility(View.INVISIBLE);
				
				getRegattaFragment().setChangePhotoButtonHidden(false);
			}
		});
    }

    @Override
    protected void onResume() {
        TextView competitorNameTextView = (TextView)findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitorName);
        
        TextView sailIdTextView = (TextView)findViewById(R.id.sail_id);
        sailIdTextView.setText(competitorSailId);
        
        
        ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        //String flagStr = String.format("%s.png", countryCode);
        String uri = "@drawable/" + competitorCountryCode.toLowerCase(Locale.getDefault());
        
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        
		try {
			Drawable res = getResources().getDrawable(imageResource);
			flagImageView.setImageDrawable(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getRegattaFragment().setChangePhotoButtonHidden(true);
		
		if (eventImageUrl != null)
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
	        	new DownloadImageTask(imageView).execute(eventImageUrl);
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    private  File getMediaFile(){
    	
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
            String mImageName="MI_"+ leaderboardName +".png";
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
}

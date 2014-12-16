package com.sap.sailing.android.tracking.app.ui.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

public class RegattaFragment extends BaseFragment implements OnClickListener {

	private final String TAG = RegattaFragment.class.toString();

	private final int CAMERA_REQUEST_CODE = 3118;
	private final int SELECT_PHOTO_REQUEST_CODE = 1693;
	private final int IMAGE_MAX_SIZE = 2000;
	
	private final String CAMERA_TEMP_FILE = "cameraTempFile";
	
	
	private boolean showingThankYouNote;

	private TimerRunnable timer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_regatta, container, false);

		Button startTrackingButton = (Button) view.findViewById(R.id.start_tracking);
		startTrackingButton.setOnClickListener(this);

		Button changePhotoButton = (Button) view.findViewById(R.id.change_photo_button);
		changePhotoButton.setOnClickListener(this);

		ImageButton addPhotoButton = (ImageButton) view.findViewById(R.id.add_photo_button);
		addPhotoButton.setOnClickListener(this);

		TextView addPhotoText = (TextView) view.findViewById(R.id.add_photo_text);
		addPhotoText.setOnClickListener(this);

		return view;
	}

	private void checkAndSwitchToThankYouScreenIfRegattaOver() {
		RegattaActivity regattaActivity = (RegattaActivity) getActivity();
		long regattaEnd = regattaActivity.getEvent().endMillis;

		if (System.currentTimeMillis() > regattaEnd) {
			switchToThankYouScreen();
		}
	}
	
	/**
	 * If the regatta started, don't display the countdown any more.
	 */
	private void checkAndHideCountdownIfRegattaIsInProgress() {
		TextView textView = (TextView)getActivity().findViewById(R.id.regatta_starts_in);
		
		RegattaActivity regattaActivity = (RegattaActivity) getActivity();
		long regattaStart = regattaActivity.getEvent().startMillis;
		
		LinearLayout threeBoxesLayout = (LinearLayout) getActivity()
				.findViewById(R.id.three_boxes_regatta_starts);
		
		if (System.currentTimeMillis() > regattaStart)
		{
			textView.setText(getString(R.string.regatta_in_progress));
			threeBoxesLayout.setVisibility(View.INVISIBLE);	
		}
		else
		{
			textView.setText(getString(R.string.regatta_starts_in));
			threeBoxesLayout.setVisibility(View.VISIBLE);
		}
	}

	public boolean isShowingBigCheckoutButton() {
		return showingThankYouNote;
	}
	
	private void switchToThankYouScreen() {
		showingThankYouNote = true;

		RelativeLayout startsInLayout = (RelativeLayout) getActivity()
				.findViewById(R.id.start_date_layout);
		startsInLayout.setVisibility(View.INVISIBLE);

		Button startTrackingButton = (Button) getActivity().findViewById(R.id.start_tracking);
		startTrackingButton.setBackgroundColor(getActivity().getResources().getColor(R.color.sap_yellow));
		startTrackingButton.setText(getActivity().getString(R.string.close));

		TextView bottomAnnouncement = (TextView) getActivity().findViewById(R.id.bottom_announcement);
		bottomAnnouncement.setVisibility(View.INVISIBLE);

		RelativeLayout thankYouLayout = (RelativeLayout) getActivity().findViewById(R.id.thank_you_layout);
		thankYouLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		timer = new TimerRunnable();
		timer.start();
		checkAndSwitchToThankYouScreenIfRegattaOver();
		checkAndHideCountdownIfRegattaIsInProgress();
	}

	@Override
	public void onPause() {
		super.onPause();
		timer.stop();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start_tracking:
			if (showingThankYouNote) {
				RegattaActivity regattaActivity = (RegattaActivity) getActivity();
				regattaActivity.checkout();
			} else {
				startTrackingActivity();
			}
			break;
		case R.id.add_photo_button:
			showChooseExistingPictureOrTakeNewPhotoAlert();
			break;
		case R.id.add_photo_text:
			showChooseExistingPictureOrTakeNewPhotoAlert();
			break;
		case R.id.change_photo_button:
			showChooseExistingPictureOrTakeNewPhotoAlert();
			break;
		default:
			break;
		}
	}

	public void setChangePhotoButtonHidden(boolean hidden) {
		LinearLayout changePhotoLayout = (LinearLayout) getActivity()
				.findViewById(R.id.change_photo);
		if (hidden) {
			changePhotoLayout.setVisibility(View.INVISIBLE);
		} else {
			changePhotoLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Ask user if he wants to take a new picture or select an existing one.
	 */
	private void showChooseExistingPictureOrTakeNewPhotoAlert() {
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.add_photo_select)
				.setMessage(
						R.string.do_you_want_to_choose_existing_img_or_take_a_new_one)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.existing_image,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								pickExistingImage();
							}
						})
				.setNegativeButton(R.string.take_photo,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showTakePhotoActivity();
							}
						}).create();

		dialog.show();
	}

	private void pickExistingImage() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);
	}

	private void showTakePhotoActivity() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File photoFile = ((RegattaActivity)getActivity()).getImageFile(CAMERA_TEMP_FILE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			RegattaActivity activity = (RegattaActivity) getActivity();

			if (requestCode == CAMERA_REQUEST_CODE) {
				File photoFile = ((RegattaActivity)getActivity()).getImageFile(CAMERA_TEMP_FILE);
				Bitmap photo;
				try {
					photo = decodeUri(Uri.fromFile(photoFile));
					activity.updateLeaderboardPictureChosenByUser(photo);
				} catch (FileNotFoundException e) {
					if (BuildConfig.DEBUG)
					{
						ExLog.i(getActivity(), TAG, "update photo, file not found: " + photoFile);
					}
				} catch (IOException e) {
					if (BuildConfig.DEBUG)
					{
						ExLog.i(getActivity(), TAG, "update photo, io exception: " + e.getMessage());
					}
				} finally {
					((RegattaActivity)getActivity()).deleteFile(CAMERA_TEMP_FILE);
				}
			} else if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
				Uri selectedImage = data.getData();
				
				try {
					Bitmap photo = decodeUri(selectedImage);
					activity.updateLeaderboardPictureChosenByUser(photo);
				} catch (FileNotFoundException e) {
					ExLog.e(getActivity(), TAG,
							"File not found exception after picking image from gallery");
				} catch (IOException e) {
					if (BuildConfig.DEBUG)
					{
						ExLog.i(getActivity(), TAG, "update photo, io exception: " + e.getMessage());
					}
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * Decode image with maximum size 
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	private Bitmap decodeUri(Uri uri) throws IOException{
	    Bitmap bitmap = null;

	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;

	    InputStream fis = getActivity().getContentResolver().openInputStream(uri);
	    BitmapFactory.decodeStream(fis, null, options);
	    fis.close();

	    int scale = 1;
	    if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
	        scale = (int)Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE / 
	           (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
	    }

	    BitmapFactory.Options options2 = new BitmapFactory.Options();
	    options2.inSampleSize = scale;
	    fis = getActivity().getContentResolver().openInputStream(uri);
	    bitmap = BitmapFactory.decodeStream(fis, null, options2);
	    fis.close();

	    return bitmap;
	}
	


	private void startTrackingActivity() {
		RegattaActivity regattaActivity = (RegattaActivity) getActivity();
		Intent intent = new Intent(getActivity(), TrackingActivity.class);
		intent.putExtra(
				getString(R.string.tracking_activity_event_id_parameter),
				regattaActivity.getEvent().id);
		getActivity().startActivity(intent);
	}

	private void timerFired() {
		RegattaActivity regattaActivity = (RegattaActivity) getActivity();
		updateCountdownTimer(regattaActivity.getEvent().startMillis);
	}

	private void updateCountdownTimer(long startTime) {
		long diff = startTime - System.currentTimeMillis();
		if (diff < 0) // start of event is in the past
		{
			setCountdownTime(0, 0, 0);
		} else {
			int days = (int) TimeUnit.MILLISECONDS.toDays(diff);
			int hours = (int) TimeUnit.MILLISECONDS.toHours(diff) - (days * 24);
			int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff)
					- (hours * 60) - (days * 24 * 60);

			setCountdownTime(days, hours, minutes);
		}
	}

	private void setCountdownTime(int days, int hours, int minutes) {
		TextView daysTextView = (TextView) getActivity().findViewById(R.id.starts_in_days);
		TextView hoursTextView = (TextView) getActivity().findViewById(R.id.starts_in_hours);
		TextView minutesTextView = (TextView) getActivity().findViewById(R.id.starts_in_minutes);

		TextView daysTextViewLabel = (TextView) getActivity().findViewById(R.id.starts_in_days_label);
		TextView hoursTextViewLabel = (TextView) getActivity().findViewById(R.id.starts_in_hours_label);
		TextView minutesTextViewLabel = (TextView) getActivity().findViewById(R.id.starts_in_minutes_label);

		daysTextView.setText(String.format("%02d", days));
		hoursTextView.setText(String.format("%02d", hours));
		minutesTextView.setText(String.format("%02d", minutes));

		if (days == 1) {
			daysTextViewLabel.setText(R.string.day);
		} else {
			daysTextViewLabel.setText(R.string.days);
		}

		if (hours == 1) {
			hoursTextViewLabel.setText(R.string.hour);
		} else {
			hoursTextViewLabel.setText(R.string.hours);
		}

		if (minutes == 1) {
			minutesTextViewLabel.setText(R.string.minute);
		} else {
			minutesTextViewLabel.setText(R.string.minutes);
		}
	}

	private class TimerRunnable implements Runnable {

		public Thread t;
		public volatile boolean running = true;

		public void start() {
			running = true;
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}

		@Override
		public void run() {
			while (running) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						timerFired();
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stop() {
			running = false;
		}
	}

}

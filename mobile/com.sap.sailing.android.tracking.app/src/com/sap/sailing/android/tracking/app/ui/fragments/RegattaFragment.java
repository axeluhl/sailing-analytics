package com.sap.sailing.android.tracking.app.ui.fragments;

import java.io.FileNotFoundException;
import java.io.InputStream;

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
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

public class RegattaFragment extends BaseFragment implements OnClickListener {

	private final String TAG = RegattaFragment.class.toString();
	
	private final int CAMERA_REQUEST_CODE = 3118;
	private final int SELECT_PHOTO_REQUEST_CODE = 1693;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_regatta, container,
				false);

		Button startTrackingButton = (Button) view
				.findViewById(R.id.start_tracking);
		startTrackingButton.setOnClickListener(this);

		ImageButton changePhotoButton = (ImageButton) view
				.findViewById(R.id.change_photo_button);
		changePhotoButton.setOnClickListener(this);

		ImageButton addPhotoButton = (ImageButton) view
				.findViewById(R.id.add_photo_button);
		addPhotoButton.setOnClickListener(this);

		TextView addPhotoText = (TextView) view
				.findViewById(R.id.add_photo_text);
		addPhotoText.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start_tracking:
			showTrackingActivity();
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
				.setMessage(R.string.do_you_want_to_choose_existing_img_or_take_a_new_one)
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
						})
				.create();

		dialog.show();
	}
	
	private void pickExistingImage()
	{
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);   
	}

	private void showTakePhotoActivity() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK)
		{
			RegattaActivity activity = (RegattaActivity) getActivity();
			
			if (requestCode == CAMERA_REQUEST_CODE) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				activity.updatePictureChosenByUser(photo);
			}
			else if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
				Uri selectedImage = data.getData();
	            InputStream imageStream;
				try {
					imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
					Bitmap photo = BitmapFactory.decodeStream(imageStream);
					activity.updatePictureChosenByUser(photo);
				} catch (FileNotFoundException e) {
					ExLog.e(getActivity(), TAG, "File not found exception after picking image from gallery");
					return;
				}
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showTrackingActivity() {
		Intent intent = new Intent(getActivity(), TrackingActivity.class);
		getActivity().startActivity(intent);
	}

}

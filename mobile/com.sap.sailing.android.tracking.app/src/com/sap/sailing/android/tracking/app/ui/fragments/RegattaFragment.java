package com.sap.sailing.android.tracking.app.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

public class RegattaFragment extends BaseFragment implements OnClickListener {

	private final int CAMERA_REQUEST_CODE = 3118;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_regatta, container, false);
		
		Button startTrackingButton = (Button) view.findViewById(R.id.start_tracking);
		startTrackingButton.setOnClickListener(this);
		
		ImageButton changePhotoButton = (ImageButton) view.findViewById(R.id.change_photo_button);
		changePhotoButton.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start_tracking:
			showTrackingActivity();
			break;
		case R.id.change_photo_button:
			showTakePhotoActivity();
			break;
		default:
			break;
		}
	}
	
	public void setChangePhotoButtonHidden(boolean hidden)
	{
		LinearLayout changePhotoLayout = (LinearLayout) getActivity().findViewById(R.id.change_photo);
		if (hidden)
		{
			changePhotoLayout.setVisibility(View.INVISIBLE);
		}
		else
		{
			changePhotoLayout.setVisibility(View.VISIBLE);
		}
	}
	
	private void showTakePhotoActivity()
	{
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputImageFileUri);

        
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == CAMERA_REQUEST_CODE) {
	        Bitmap photo = (Bitmap) data.getExtras().get("data");
	        RegattaActivity activity = (RegattaActivity)getActivity();
	        activity.updatePictureChosenByUser(photo);
	    }
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void showTrackingActivity() {
		Intent intent = new Intent(getActivity(), TrackingActivity.class);
		getActivity().startActivity(intent);
	}

}

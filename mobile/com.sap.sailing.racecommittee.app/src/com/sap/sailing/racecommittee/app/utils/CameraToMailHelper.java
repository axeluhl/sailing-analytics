package com.sap.sailing.racecommittee.app.utils;

import java.io.File;

import com.sap.sailing.racecommittee.app.AppConstants;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class CameraToMailHelper {

    public void sendPictureToMail(String[] recipients, Context context) {
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + AppConstants.ApplicationFolder);
        imageDirectory.mkdirs();
        File imageFile = new File(imageDirectory, "image.jpg");
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        
        
        /*Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
        //intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Send mail..."));*/
    }
    
}

package com.sap.sailing.racecommittee.app.ui.views;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// Deprecation of Camera API.
// New Camera API in Android 5.0
// New handling for camera in new RC App.
@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private AudioManager audioManager;

    public CameraView(Context context) {
        super(context);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void snap(final PictureCallback pictureHandler) {
        camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                camera.takePicture(shutterSoundCallback, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        pictureHandler.onPictureTaken(data, camera);
                        camera.startPreview();
                    }
                });
            }
        });
    }

    public void setCamera(Camera newCamera) {
        camera = newCamera;
        if (camera == null) {
            return;
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            requestLayout();
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview", e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (camera != null) {
            camera.stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private ShutterCallback shutterSoundCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);
        }
    };
}

package com.sap.sailing.android.tracking.app.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassManager implements SensorEventListener {
	//private final static String TAG = CompassManager.class.getName();
	
	private static CompassManager mInstance;
	private Context mContext;

	private SensorManager sensorManager;
	private Sensor gsensor;
	private Sensor msensor;
	private float[] mGravity = new float[3];
	private float[] mGeomagnetic = new float[3];
	private float azimuth = 0f;
	
	private MagneticHeadingListener listener;
	
	public static synchronized CompassManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new CompassManager(context);
		}
		return mInstance;
	}
	
	private CompassManager(Context context) {
		mContext = context;
		sensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	public void registerListener(MagneticHeadingListener listener)
	{
		this.listener = listener;
		start();
	}
	
	public void unregisterListener()
	{
		stop();
		this.listener = null;
	}

	public void start() {
		sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void stop() {
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		final float alpha = 0.97f;

		if (listener == null) {
			return;
		}
		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
				mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
				mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
			}

			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
				mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
				mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
			}

			float R[] = new float[9];
			float I[] = new float[9];

			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = (float) Math.toDegrees(orientation[0]); // orientation
				azimuth = (azimuth + 360) % 360;
				listener.magneticHeadingUpdated(azimuth);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	public interface MagneticHeadingListener {
		public void magneticHeadingUpdated(float heading);
	}
}

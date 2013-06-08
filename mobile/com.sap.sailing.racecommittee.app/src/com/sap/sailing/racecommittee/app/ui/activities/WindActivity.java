package com.sap.sailing.racecommittee.app.ui.activities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;

public class WindActivity extends BaseActivity implements CompassDirectionListener, LocationListener {

    CompassView compassView;
    EditText windDirection;
    EditText windSpeed;
    EditText latitude;
    EditText longitude;
    SeekBar seekWindSpeed;
    Button btnGps;
    Button btnSend;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wind_view);

        compassView = (CompassView) findViewById(R.id.compassView1);
        windDirection = (EditText) findViewById(R.id.et_wind_direction);
        windSpeed = (EditText) findViewById(R.id.et_wind_speed);
        latitude = (EditText) findViewById(R.id.et_position_lat);
        longitude = (EditText) findViewById(R.id.et_position_lon);
        seekWindSpeed = (SeekBar) findViewById(R.id.sb_wind_speed);
        btnGps = (Button) findViewById(R.id.btn_wind_query_gps);
        btnSend = (Button) findViewById(R.id.btn_wind_send);

        seekWindSpeed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = round(progress / 10.0f, 1);
                windSpeed.setText(speedFormat.format(speed));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnGps.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) WindActivity.this
                        .getSystemService(Context.LOCATION_SERVICE);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                latitude.setText(loc.getLatitude() + "");
                longitude.setText(loc.getLongitude() + "");
            }
        });

        btnSend.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try{
                InMemoryDataStore.INSTANCE.setLastWindSpeed(Double.valueOf(windSpeed.getText().toString().replace(",", ".")));
                InMemoryDataStore.INSTANCE.setLastWindDirection(Integer.valueOf(windDirection.getText().toString()));
                InMemoryDataStore.INSTANCE.setLastLatitude(Double.valueOf(latitude.getText().toString()));
                InMemoryDataStore.INSTANCE.setLastLongitude(Double.valueOf(longitude.getText().toString()));
                }catch (NumberFormatException nfe){
                    Toast.makeText(WindActivity.this, "The entered information is not valid", Toast.LENGTH_LONG).show();
                    ExLog.i(this.getClass().getCanonicalName(), nfe.getMessage());
                }
                finish();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        
        windSpeed.setText(InMemoryDataStore.INSTANCE.getLastWindSpeed()+"");
        windDirection.setText(InMemoryDataStore.INSTANCE.getLastWindDirection()+"");
        latitude.setText(InMemoryDataStore.INSTANCE.getLastLatitude()+"");
        longitude.setText(InMemoryDataStore.INSTANCE.getLastLongitude()+"");
    }

    DecimalFormat speedFormat = new DecimalFormat("##.0");
    DecimalFormat directionFormat = new DecimalFormat("###");

    @Override
    protected void onStart() {
        super.onStart();

        compassView.setDirectionListener(this);
    }

    public void onDirectionChanged(float degree) {
        float direction = round(degree, 0);
        windDirection.setText(directionFormat.format(direction));
    }

    public static float round(float unrounded, int precision) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }
    
    @Override
    protected void onPause() {
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}

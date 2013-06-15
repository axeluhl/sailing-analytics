package com.sap.sailing.racecommittee.app.ui.activities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.Intent;
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

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;

public class WindActivity extends BaseActivity implements CompassDirectionListener, LocationListener {
    
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;
    
    CompassView compassView;
    EditText windBearingEditText;
    EditText windSpeedEditText;
    EditText latitudeEditText;
    EditText longitudeEditText;
    SeekBar windSpeedSeekBar;
    Button sendButton;
    LocationManager locationManager;
    Location currentLocation;
    DecimalFormat speedFormat;
    DecimalFormat bearingFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wind_view);

        compassView = (CompassView) findViewById(R.id.compassView);
        windBearingEditText = (EditText) findViewById(R.id.editTextWindDirection);
        windSpeedEditText = (EditText) findViewById(R.id.editTextWindSpeed);
        latitudeEditText = (EditText) findViewById(R.id.et_position_lat);
        longitudeEditText = (EditText) findViewById(R.id.et_position_lon);
        windSpeedSeekBar = (SeekBar) findViewById(R.id.seekbar_wind_speed);
        sendButton = (Button) findViewById(R.id.btn_wind_send);
        
        currentLocation = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_SEC, EVERY_POSITION_CHANGE, this);

        windSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = round(progress / 10.0f, 1);
                windSpeedEditText.setText(speedFormat.format(speed));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    Wind wind = getResultingWindFix();
                    if (wind == null) {
                        Toast.makeText(WindActivity.this, R.string.wind_location_or_fields_not_valid, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent resultData = new Intent();
                    resultData.putExtra(AppConstants.EXTRAS_WIND_FIX, wind);
                    setResult(RESULT_OK, resultData);
                    finish();
                } catch (NumberFormatException nfe) {
                    Toast.makeText(WindActivity.this, R.string.wind_speed_direction_not_a_valid_number, Toast.LENGTH_LONG).show();
                    ExLog.i(this.getClass().getCanonicalName(), nfe.getMessage());
                }
            }

        });
        
        speedFormat = new DecimalFormat("##.0");
        bearingFormat = new DecimalFormat("###");
        
        windSpeedEditText.setText(speedFormat.format(AppPreferences.getWindSpeed(getBaseContext())));
        windBearingEditText.setText(bearingFormat.format(AppPreferences.getWindBearing(getBaseContext())));
    }

    @Override
    protected void onStart() {
        super.onStart();

        compassView.setDirectionListener(this);
    }

    @Override
    public void onDirectionChanged(float degree) {
        float direction = round(degree, 0);
        windBearingEditText.setText(bearingFormat.format(direction));
    }

    @Override
    protected void onPause() {
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        latitudeEditText.setText(String.valueOf(location.getLatitude()));
        longitudeEditText.setText(String.valueOf(location.getLongitude()));
        sendButton.setEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    
    protected static float round(float unrounded, int precision) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }
    
    private Wind getResultingWindFix() throws NumberFormatException {
        if (currentLocation == null || windSpeedEditText.getText().toString().isEmpty() || windBearingEditText.getText().toString().isEmpty()) {
            return null;
        }
        
        Position currentPosition = new DegreePosition(currentLocation.getLatitude(), currentLocation.getLongitude());
        double windSpeed = Double.valueOf(windSpeedEditText.getText().toString());
        double windBearing = Double.valueOf(windBearingEditText.getText().toString());
        Bearing bearing = new DegreeBearingImpl(windBearing);
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(windSpeed, bearing);
        
        return new WindImpl(currentPosition, MillisecondsTimePoint.now(), speedBearing);
    }

}

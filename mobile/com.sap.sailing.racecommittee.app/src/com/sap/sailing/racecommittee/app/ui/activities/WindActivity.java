package com.sap.sailing.racecommittee.app.ui.activities;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindActivity extends SessionActivity implements CompassDirectionListener, LocationListener {

    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;

    CompassView compassView;
    EditText windBearingEditText;
    EditText windSpeedEditText;
    SeekBar windSpeedSeekBar;
    Button sendButton;
    TextView waitingForGpsTextView;
    
    LocationManager locationManager;
    Location currentLocation;
    DecimalFormat speedFormat;
    DecimalFormat bearingFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wind_view);

        compassView = (CompassView) findViewById(R.id.compassView);
        windBearingEditText = (EditText) findViewById(R.id.editTextWindDirection);
        windSpeedEditText = (EditText) findViewById(R.id.editTextWindSpeed);
        windSpeedSeekBar = (SeekBar) findViewById(R.id.seekbar_wind_speed);
        sendButton = (Button) findViewById(R.id.btn_wind_send);
        waitingForGpsTextView = (TextView) findViewById(R.id.textWaitingForGPS);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_SEC, EVERY_POSITION_CHANGE, this);

        windSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = round(progress / 10.0f, 1);
                int displayedValue = Integer.getInteger(windSpeedEditText.getText().toString(), 0).intValue();
                int progressValue = Float.valueOf(speed).intValue();
                if (displayedValue != progressValue) {
                    windSpeedEditText.setText(speedFormat.format(speed));
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sendButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    Wind wind = getResultingWindFix();
                    if (wind == null) {
                        Toast.makeText(WindActivity.this, R.string.wind_location_or_fields_not_valid, Toast.LENGTH_LONG).show();
                        return;
                    }
                    saveEntriesInPreferences(wind);
                    Intent resultData = new Intent();
                    resultData.putExtra(AppConstants.EXTRAS_WIND_FIX, wind);
                    setResult(RESULT_OK, resultData);
                    finish();
                } catch (NumberFormatException nfe) {
                    Toast.makeText(WindActivity.this, R.string.wind_speed_direction_not_a_valid_number, Toast.LENGTH_LONG).show();
                    ExLog.i(WindActivity.this, this.getClass().getCanonicalName(), nfe.getMessage());
                }
            }

        });
        sendButton.setEnabled(false);

        windBearingEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String windBearingText = windBearingEditText.getText().toString();
                    if (windBearingText.length() > 0) {
                        compassView.setDirection(Float.valueOf(windBearingText));
                    }
                    
                }
            }
        });
        windSpeedEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String windSpeedText = windSpeedEditText.getText().toString();
                    if (windSpeedText.length() > 0) {
                        windSpeedSeekBar.setProgress(Double.valueOf(windSpeedText).intValue() * 10);
                    }
                }
            }
        });
        speedFormat = new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US));
        bearingFormat = new DecimalFormat("###", new DecimalFormatSymbols(Locale.US));

        AppPreferences preferences = AppPreferences.on(getApplicationContext());
        double enteredWindSpeed = preferences.getWindSpeed();
        windSpeedSeekBar.setProgress(Double.valueOf(enteredWindSpeed).intValue() * 10);
        windSpeedEditText.setText(speedFormat.format(enteredWindSpeed));
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();
        compassView.setDirection((float)enteredWindBearingFrom);
        windBearingEditText.setText(bearingFormat.format(enteredWindBearingFrom));
    }

    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.wind_gps_is_disabled)
        .setCancelable(false)
        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        })
        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    protected void saveEntriesInPreferences(Wind wind) {
        // Wind.getBearing() returns a value that assumes that the wind flows in that direction
        // But for this app we need to display the direction the wind is coming from
        AppPreferences preferences = AppPreferences.on(getApplicationContext());
        preferences.setWindBearingFromDirection(wind.getBearing().reverse().getDegrees());
        preferences.setWindSpeed(wind.getKnots());
    }

    @Override
    public void onStart() {
        super.onStart();

        compassView.setDirectionListener(this);
    }

    @Override
    public void onDirectionChanged(float degree) {
        float direction = round(degree, 0);
        windBearingEditText.setText(bearingFormat.format(direction));
    }

    @Override
    public void onPause() {
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        waitingForGpsTextView.setText("");
        sendButton.setEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_SEC, EVERY_POSITION_CHANGE, this);
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
        double windSpeed = Double.valueOf(windSpeedEditText.getText().toString().replace(",", "."));
        double windBearing = Double.valueOf(windBearingEditText.getText().toString());
        Bearing bearing_from = new DegreeBearingImpl(windBearing);
        // this is not a standard bearing but the direction where the wind comes from, needs to be converted
        // to match the assumption that a bearing is always the direction the wind flows to
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(windSpeed, bearing_from.reverse());
        return new WindImpl(currentPosition, MillisecondsTimePoint.now(), speedBearing);
    }

}

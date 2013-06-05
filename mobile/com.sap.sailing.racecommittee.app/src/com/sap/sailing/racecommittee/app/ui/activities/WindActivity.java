package com.sap.sailing.racecommittee.app.ui.activities;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;

public class WindActivity extends BaseActivity implements CompassDirectionListener {

	CompassView compassView;
	EditText windDirection;
	EditText windSpeed;
	SeekBar seekWindSpeed;
	Button btnGps;
	Button btnSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wind_view);
		
		compassView = (CompassView) findViewById(R.id.compassView1);
		windDirection = (EditText) findViewById(R.id.et_wind_direction);
		windSpeed = (EditText) findViewById(R.id.et_wind_speed);
		seekWindSpeed = (SeekBar) findViewById(R.id.sb_wind_speed);
		btnGps = (Button) findViewById(R.id.btn_wind_query_gps);
		btnSend = (Button) findViewById(R.id.btn_wind_send);
		
		seekWindSpeed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float speed = round(progress / 10.0f, 1);
				windSpeed.setText(speedFormat.format(speed));
			}

			public void onStartTrackingTouch(SeekBar seekBar) { }

			public void onStopTrackingTouch(SeekBar seekBar) { }
		});
		
		btnGps.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				/*LocationManager locationManager = 
						(LocationManager) WindActivity.this.getSystemService(Context.LOCATION_SERVICE);
				Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Date time = new Date(loc.getTime());
				Log.i("Wind", String.format("GPS %s : %s@%s", time.toString(), 
						loc.getLatitude(), loc.getLongitude()));*/
			}
		});
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Toast.makeText(WindActivity.this, "Not implemented yet.", Toast.LENGTH_SHORT).show();
			}
		});
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
	
	public static float round(float unrounded, int precision)
	{
	    BigDecimal decimal = new BigDecimal(unrounded);
	    BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
	    return round.floatValue();
	}

	
}

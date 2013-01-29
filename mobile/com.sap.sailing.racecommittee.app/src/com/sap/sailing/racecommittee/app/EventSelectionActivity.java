package com.sap.sailing.racecommittee.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

public class EventSelectionActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_selection);
		
		Downloader downloader = new Downloader(this);
		downloader.execute();
	}

	public void show(String result) {
		TextView tv = (TextView) findViewById(R.id.textView1);
		
		if (result != null) {
		
		JsonDeserializer<Event> deserializer = new EventJsonDeserializer(
				new VenueJsonDeserializer(
						new CourseAreaJsonDeserializer()));
		
		JSONParser p = new JSONParser();
		try {
			JSONArray o = (JSONArray) p.parse(result);
			for (Object e : o) {
				
				/*JSONObject json = (JSONObject) e;
				RaceCommitteeEventDeserializerFactory factory = new RaceCommitteeEventDeserializerFactory();
				RaceCommitteeEvent event = factory.getDeserializer(json).deserialize(json);*/
				
				Event event = deserializer.deserialize((JSONObject)e);
				tv.setText(event.getName());
			}
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		
		tv.setText("Fehler");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_event_selection, menu);
		return true;
	}

	private class Downloader extends AsyncTask<Object, Integer, String> {

		private EventSelectionActivity activity;
		
		public Downloader(EventSelectionActivity activity) {
			this.activity = activity;
		}
		
		@Override
		protected String doInBackground(Object... params) {
			try {
				URL url = new URL("http://10.0.2.2:8888/sailingserver/rc/events");
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(5000);
				try {
					BufferedReader r = new BufferedReader(
							new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder total = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
					    total.append(line);
					}
					return total.toString();
				} finally {
					urlConnection.disconnect();
				}
			} catch (IOException ioe) {
				ioe.toString();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			activity.show(result);
		}

	}
}

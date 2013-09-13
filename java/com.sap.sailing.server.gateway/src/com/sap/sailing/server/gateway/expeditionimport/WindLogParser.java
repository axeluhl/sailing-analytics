package com.sap.sailing.server.gateway.expeditionimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;

public class WindLogParser {

	/**
	 * Collects partial wind data until everything is there to create a Wind instance.
	 * @author D047974
	 *
	 */
	private static class WindBuffer {

		/**
		 * The 1900 Date System http://support.microsoft.com/kb/180162/en-us
		 */
		private static final Calendar EXCEL_EPOCH_START = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		static {
			EXCEL_EPOCH_START.set(1899, Calendar.DECEMBER, 30, 0, 0, 0);
		}
		private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

		private TimePoint timePoint;
		private Position position;
		private SpeedWithBearing trueWindSpeedWithBearing;

		public void updateTime(String time_ExcelEpoch) {
			double excelTimeStamp = Double.parseDouble(time_ExcelEpoch);
			long millisecondsSinceExcelEpochStart = (long) (MILLISECONDS_PER_DAY * excelTimeStamp);
			this.timePoint = new MillisecondsTimePoint(EXCEL_EPOCH_START.getTimeInMillis() + millisecondsSinceExcelEpochStart);

		}

		public void updateWindData(String trueWindSpeedData, String trueWindDirectionData) {
			if (!trueWindSpeedData.trim().isEmpty() && !trueWindDirectionData.trim().isEmpty()) {

				double trueWindSpeed = Double.parseDouble(trueWindSpeedData);
				double trueWindDirection = Double.parseDouble(trueWindDirectionData);
				Bearing trueWindBearing = new DegreeBearingImpl(trueWindDirection + 180);
				this.trueWindSpeedWithBearing = new KnotSpeedWithBearingImpl(trueWindSpeed, trueWindBearing);
			}
		}

		public void updatePosition(String latData, String lonData) {
			if (!latData.trim().isEmpty() && !lonData.trim().isEmpty()) {
				double lat = Double.parseDouble(latData);
				double lon = Double.parseDouble(lonData);

				this.position = new DegreePosition(lat, lon);
			}
		}

		public Wind createWindIfReady() {
			if ((timePoint != null) && (position != null) && (trueWindSpeedWithBearing != null)) {
				WindImpl result = new WindImpl(position, timePoint, trueWindSpeedWithBearing);
				reset();
				return result;
			} else {
				return null;
			}
		}

		private void reset() {
			timePoint = null;
			position = null;
			trueWindSpeedWithBearing = null;
		}

	}

	private static final int CSV_INDEX_TIME_STAMP = 0;
	private static final int CSV_INDEX_TRUE_WIND_SPEED = 5;
	private static final int CSV_INDEX_TRUE_WIND_DIRECTION = 6;
	private static final int CSV_INDEX_LAT = 37;
	private static final int CSV_INDEX_LONG = 38;
	
	private static final int CSV_MAX_INDEX = Collections.max(Arrays.asList(CSV_INDEX_TIME_STAMP, CSV_INDEX_TRUE_WIND_SPEED, CSV_INDEX_TRUE_WIND_DIRECTION, CSV_INDEX_LAT, CSV_INDEX_LONG));

	private static final Pattern LINE_PATTERN = Pattern.compile(",");

	/**
	 * See ExpeditionMessage, UDPExpeditionReceiver
	 * @param windStream
	 * @return
	 * @throws IOException
	 */
	public static List<Wind> importWind(InputStream windStream) throws IOException {
		BufferedReader csvReader = new BufferedReader(new InputStreamReader(windStream));
		String headerLine = csvReader.readLine();
		assertHeaderFormat(headerLine);

		List<Wind> result = new ArrayList<Wind>();
		String dataLine;
		WindBuffer windBuffer = new WindBuffer();
		while ((dataLine = csvReader.readLine()) != null) {
			String[] data = parseDataLine(dataLine);

			if (data != null) {
				windBuffer.updateTime(data[CSV_INDEX_TIME_STAMP]);
				windBuffer.updateWindData(data[CSV_INDEX_TRUE_WIND_SPEED], data[CSV_INDEX_TRUE_WIND_DIRECTION]);
				windBuffer.updatePosition(data[CSV_INDEX_LAT], data[CSV_INDEX_LONG]);

				Wind wind = windBuffer.createWindIfReady();
				if (wind != null) {
					result.add(wind);
				}
			}
		}

		return result;
	}

	private static void assertHeaderFormat(String headerLine) {
		//supposed to be Utc,Bsp,Awa,Aws,Twa,Tws,Twd,Rudder2,Leeway,Set,Drift,Hdg,AirTmp,SeaTmp,Baro,Depth,Heel,Trim,Rudder,Tab,Forestay,Downhaul,MastAng,FrstyLen,MastButt,LoadStbd,LoadPort,Rake,Volts,ROT,GpsQual,GpsPDOP,GpsNum,GpsAge,GpsAltitude,GpsGeoSep,GpsMode,Lat,Lon,Cog,Sog,DiffStn,Error,StbRunner,PrtRunner,Vang,Traveller,MainSheet,KeelAng,KeelHt,CanardH,OilPres,RPM1,RPM2,Dagger Bd,Boom Pos,DistToLn,RchTmToLn,RchDtToLn,GPS Time,Downhaul2,MkLat,MkLon,PortLat,PortLon,StbdLat,StbdLon,GPS HPE,RH,LeadPt,LeadSb,BkStay,User 0,User 1,User 2,User 3,User 4,User 5,User 6,User 7,User 8,User 9,User 10,User 11,User 12,User 13,User 14,User 15,User 16,User 17,User 18,User 19,User 20,User 21,User 22,User 23,User 24,User 25,User 26,User 27,User 28,User 29,User 30,User 31,TmToGun,TmToLn,TmToBurn,BelowLn,GunBlwLn,WvSigHt,WvSigPd,WvMaxHt,WvMaxPd,Slam,Motion
		String[] columns = LINE_PATTERN.split(headerLine);
		if ("Utc".equals(columns[CSV_INDEX_TIME_STAMP]) && "Tws".equals(columns[CSV_INDEX_TRUE_WIND_SPEED]) && "Twd".equals(columns[CSV_INDEX_TRUE_WIND_DIRECTION]) && "Lat".equals(columns[CSV_INDEX_LAT]) && "Lon".equals(columns[CSV_INDEX_LONG])) {
			return;
		} else {
			throw new RuntimeException("Unexpected csv header for Expedition wind import: " + headerLine);
		}
	}

	private static String[] parseDataLine(String dataLine) {
		String[] data = LINE_PATTERN.split(dataLine);
		if (data.length >= CSV_MAX_INDEX) {
			//        	if (!data[CSV_INDEX_TIME_STAMP].trim().isEmpty() 
			//        			&& !data[CSV_INDEX_TRUE_WIND_SPEED].trim().isEmpty() 
			//        			&& !data[CSV_INDEX_TRUE_WIND_DIRECTION].trim().isEmpty() 
			//        			&& !data[CSV_INDEX_LAT].trim().isEmpty() 
			//        			&& !data[CSV_INDEX_LONG].trim().isEmpty()) {
			return data;
			//        	}
		}
		//else
		return null;
	}
}

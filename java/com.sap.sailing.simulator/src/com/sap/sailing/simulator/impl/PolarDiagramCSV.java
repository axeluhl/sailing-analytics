package com.sap.sailing.simulator.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;

public class PolarDiagramCSV extends PolarDiagramBase {

	public PolarDiagramCSV(String inputFile) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream csvFile = cl.getResourceAsStream(inputFile);
		InputStreamReader isr = new InputStreamReader(csvFile);
		BufferedReader bfr = new BufferedReader(isr);

		List<Speed> velocity = new ArrayList<Speed>();
		List<Bearing> beatAngles = new ArrayList<Bearing>();
		List<Speed> beatVMG = new ArrayList<Speed>();
		Map<Bearing, List<Speed>> speeds = new HashMap<Bearing, List<Speed>>();
		List<Speed> runVMG = new ArrayList<Speed>();
		List<Bearing> gybeAngles = new ArrayList<Bearing>();

		String line = "";
		while (true) {
			line = bfr.readLine();
			if (line == null)
				break;
			String[] elements = line.split(",");
			elements[0] = elements[0].replace(" ", "");
			elements[0] = elements[0].toLowerCase();
			switch (elements[0]) {
			case "windvelocity":
				for (int i = 1; i < elements.length; i++)
					velocity.add(new KnotSpeedImpl(new Double(elements[i])));
				break;
			case "beatangles":
				for (int i = 1; i < elements.length; i++)
					beatAngles.add(new DegreeBearingImpl(
							new Double(elements[i])));
				break;
			case "beatsog":
				for (int i = 1; i < elements.length; i++)
					beatVMG.add(new KnotSpeedImpl(new Double(elements[i])));
				break;
			case "runsog":
				for (int i = 1; i < elements.length; i++)
					runVMG.add(new KnotSpeedImpl(new Double(elements[i])));
				break;
			case "gybeangles":
				for (int i = 1; i < elements.length; i++)
					gybeAngles.add(new DegreeBearingImpl(
							new Double(elements[i])));
				break;
			default:
				List<Speed> sp = new ArrayList<Speed>();

				for (int i = 1; i < elements.length; i++)
					sp.add(new KnotSpeedImpl(new Double(elements[i])));
				speeds.put(new DegreeBearingImpl(new Double(elements[0])), sp);
				break;

			}
		}
		bfr.close();

		NavigableMap<Speed, NavigableMap<Bearing, Speed>> mapSpeedTable = new TreeMap<Speed, NavigableMap<Bearing, Speed>>();
		NavigableMap<Speed, Bearing> mapBeatAngles = new TreeMap<Speed, Bearing>();
		NavigableMap<Speed, Bearing> mapGybeAngles = new TreeMap<Speed, Bearing>();
		NavigableMap<Speed, Speed> mapBeatSOG = new TreeMap<Speed, Speed>();
		NavigableMap<Speed, Speed> mapGybeSOG = new TreeMap<Speed, Speed>();

		for (int i = 0; i < velocity.size(); i++) {
			NavigableMap<Bearing, Speed> speedTableLine = new TreeMap<Bearing, Speed>(
					bearingComparator);
			for (Entry<Bearing, List<Speed>> e : speeds.entrySet()) {
				speedTableLine.put(e.getKey(), e.getValue().get(i));
			}
			mapSpeedTable.put(velocity.get(i), speedTableLine);
			mapBeatAngles.put(velocity.get(i), beatAngles.get(i));
			mapGybeAngles.put(velocity.get(i), gybeAngles.get(i));
			mapBeatSOG.put(velocity.get(i), beatVMG.get(i));
			mapGybeSOG.put(velocity.get(i), runVMG.get(i));
		}
		
		setWind(new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(180)));

		super.speedTable = mapSpeedTable;
		super.beatAngles = mapBeatAngles;
		super.gybeAngles = mapGybeAngles;
		super.beatSOG = mapBeatSOG;
		super.gybeSOG = mapGybeSOG;

		for (Speed s : super.speedTable.keySet()) {

			if (super.beatAngles.containsKey(s)
					&& !super.speedTable.get(s).containsKey(
							super.beatAngles.get(s)))
				super.speedTable.get(s).put(super.beatAngles.get(s),
						super.beatSOG.get(s));

			if (super.gybeAngles.containsKey(s)
					&& !super.speedTable.get(s).containsKey(
							super.gybeAngles.get(s)))
				super.speedTable.get(s).put(super.gybeAngles.get(s),
						super.gybeSOG.get(s));

		}

	}

}

package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulatorUISelection;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class SailingSimulatorImpl implements SailingSimulator {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    private SimulationParameters simulationParameters = null;
    private Path raceCourse = null;
    private PathGeneratorTracTrac pathGenerator = null;

    private static final double WIND_SCALE = 4.5;

    // proxy configuration
    private static final String LIVE_URI = "tcp://10.18.22.156:1520";

    // no-proxy configuration
    // private static final String LIVE_URI = "tcp://germanmaster.traclive.dk:4400";

    // proxy configuration
    private static final String STORED_URI = "tcp://10.18.22.156:1521";

    // no-proxy configuration
    // private static final String STORED_URI = "tcp://germanmaster.traclive.dk:4401";

    public SailingSimulatorImpl(SimulationParameters parameters) {
        this.simulationParameters = parameters;
        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            this.initializePathGenerator(parameters, 0, 0, 0);
        }
    }

    @Override
    public void setSimulationParameters(SimulationParameters parameters, SimulatorUISelection selection) {

        this.simulationParameters = parameters;
        this.initializePathGenerator(parameters, selection.getRaceIndex(), selection.getLegIndex(), selection.getCompetitorIndex());
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    @Override
    public Map<String, Path> getAllPaths() {

        return SimulatorUtils.getSimulationPaths(this.simulationParameters);
    }

    @Override
    public Map<String, Path> getAllPathsForLeg(SimulatorUISelection selection) {

        this.setSimulationParameters(this.simulationParameters, selection);

        Map<String, Path> allPaths = new HashMap<String, Path>();
        Path gpsPath = null;
        Path gpsPathPoly = null;

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {

            Pair<Map<String, Path>, Path> result = SimulatorUtils.readLegPathsFromResources(selection.getRaceIndex(), selection.getCompetitorIndex(),
                    selection.getLegIndex());
            allPaths = result.getA();
            this.raceCourse = result.getB();

            if (allPaths != null && allPaths.isEmpty() == false && allPaths.size() == 6) {
                return allPaths;
            }

            try {
                gpsPath = this.getFromResourcesOrDownload("7#GPS Track", selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
                gpsPathPoly = this.getFromResourcesOrDownload("6#GPS Poly", selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
                this.raceCourse = this.getFromResourcesOrDownload("raceCourse", selection.getRaceIndex(), selection.getCompetitorIndex(),
                        selection.getLegIndex());
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }

            allPaths.put("6#GPS Poly", gpsPathPoly);
            allPaths.put("7#GPS Track", gpsPath);
        }

        allPaths.putAll(SimulatorUtils.getSimulationPaths(this.simulationParameters));

        if (this.simulationParameters.getMode() == SailingSimulatorUtil.measured) {
            SimulatorUtils.saveLegPathsToFiles(allPaths, this.raceCourse, selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex());
        }

        return allPaths;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
    }

    @Override
    public Map<String, Path> getAllPathsEvenTimed(long millisecondsStep, SimulatorUISelection selection) {

        Map<String, Path> allTimedPaths = new TreeMap<String, Path>();

        Map<String, Path> allPaths = selection == null ? this.getAllPaths() : this.getAllPathsForLeg(selection);

        for (Entry<String, Path> entry : allPaths.entrySet()) {

            String pathName = entry.getKey();
            Path value = entry.getValue();

            if (pathName.equals("7#GPS Track")) {
                allTimedPaths.put(pathName, value);
            } else {
                allTimedPaths.put(pathName, value.getEvenTimedPath(millisecondsStep));
            }
        }

        return allTimedPaths;
    }

    @Override
    public Path getLegGPSTrack(SimulatorUISelection selection) {
        //
        String fileName = SimulatorUtils.getFileName(selection.getRaceIndex(), selection.getCompetitorIndex(), selection.getLegIndex(), "7#GPS Track");

        Path path = (Path) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (path == null) {
            System.err.println("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");
            LOGGER.warning("[ERROR][SailingSimulatorImpl][readPathsFromResources] Cannot de-serialize path from resources/7#GPS Track.dat");

            this.pathGenerator.setSelectionParameters(selection.getLegIndex(), selection.getCompetitorIndex());
            path = this.pathGenerator.getPath();
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getLegsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, new SimulatorUISelectionImpl(0, selectedRaceIndex, 0, 0));

        List<String> legsNames = null;
        String fileName = SimulatorUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + SimulatorUtils.LEGSNAMES_DAT;

        legsNames = (List<String>) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (legsNames != null && legsNames.isEmpty() == false) {
            return legsNames;
        }

        legsNames = this.pathGenerator.getLegsNames();

        SimulatorUtils.saveStringListToFiles(legsNames, fileName);

        return legsNames;
    }

    @Override
    public List<String> getRacesNames() {

        List<String> racesNames = new ArrayList<String>();

        for (Quadruple<String, String, String, Integer> raceInfo : ConfigurationManager.INSTANCE.getRacesInfo()) {
            racesNames.add(raceInfo.getA());
        }

        return racesNames;
    }

    @Override
    public Path getLeg(int selectedCompetitorIndex, int selectedLegIndex) {

        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
        return this.pathGenerator.getPath();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getComeptitorsNames(int selectedRaceIndex) {

        this.setSimulationParameters(this.simulationParameters, new SimulatorUISelectionImpl(0, selectedRaceIndex, 0, 0));

        List<String> competitorsNames = null;
        String fileName = SimulatorUtils.getRaceID(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex)) + "_" + SimulatorUtils.COMPETITORSNAMES_DAT;

        competitorsNames = (List<String>) SimulatorUtils.readObjectFromResources("resources/" + fileName);
        if (competitorsNames != null && competitorsNames.isEmpty() == false) {
            return competitorsNames;
        }

        competitorsNames = this.pathGenerator.getComeptitorsNames();

        SimulatorUtils.saveStringListToFiles(competitorsNames, fileName);

        return competitorsNames;
    }

    private void initializePathGenerator(SimulationParameters parameters, int selectedRaceIndex, int selectedLegIndex, int selectedCompetitorIndex) {

        this.pathGenerator = new PathGeneratorTracTrac(parameters);
        this.pathGenerator.setEvaluationParameters(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex), LIVE_URI, STORED_URI, WIND_SCALE);
        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
    }

    private Path getFromResourcesOrDownload(String pathName, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws Exception {

        String fileName = SimulatorUtils.getFileName(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex, pathName);

        Path path = (Path) SimulatorUtils.readObjectFromResources(fileName);
        if (path == null) {
            if (pathName.equals("6#GPS Poly")) {
                path = this.pathGenerator.getPathPolyline(new MeterDistance(4.88));
            } else if (pathName.equals("7#GPS Track")) {
                path = this.pathGenerator.getPath();
            } else if (pathName.equals("raceCourse")) {
                path = this.pathGenerator.getRaceCourse();
            } else {
                throw new Exception("Unknown path name!");
            }
            SimulatorUtils.saveToFile(path, SimulatorUtils.getPathPrefix() + "\\src\\resources\\" + fileName);
        }

        return path;
    }
}

package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.PathType;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.RaceProperties;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.Simulator;
import com.sap.sailing.simulator.SimulatorUISelection;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;

public class SimulatorImpl implements Simulator {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    private SimulationParameters simulationParameters = null;
    private Path raceCourse = null;
    private PathGeneratorTracTrac pathGenerator = null;
    
    public SimulatorImpl(SimulationParameters parameters) {
        this.simulationParameters = parameters;
        if (this.simulationParameters.getMode() == SailingSimulatorConstants.ModeMeasured) {
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
    public Path getPath(PathType pathType) throws SparseSimulationDataException {
        PathGeneratorTreeGrow360 genTreeGrow;
        PathGeneratorOpportunistEuclidian360 genOpportunistic;
        Path path = null;
        try {
            switch (pathType) {
            case OMNISCIENT:
                genTreeGrow = new PathGeneratorTreeGrow360(this.simulationParameters); // instantiate heuristic searcher
                genTreeGrow.setEvaluationParameters(null, 0, null); // allow for arbitrary many turns
                path = genTreeGrow.getPath();
                break;
            case ONE_TURNER_LEFT:
                genTreeGrow = new PathGeneratorTreeGrow360(this.simulationParameters); // instantiate heuristic searcher
                genTreeGrow.setEvaluationParameters("L", 1, null); // start left and limit to one turn
                path = genTreeGrow.getPath();
                break;
            case ONE_TURNER_RIGHT:
                genTreeGrow = new PathGeneratorTreeGrow360(this.simulationParameters); // instantiate heuristic searcher
                genTreeGrow.setEvaluationParameters("R", 1, null); // start right and limit to one turn
                path = genTreeGrow.getPath();
                break;
            case OPPORTUNIST_LEFT:
                genOpportunistic = new PathGeneratorOpportunistEuclidian360(this.simulationParameters); // instantiate greedy searcher
                genOpportunistic.setEvaluationParameters(true); // use maxTurnTimes of 1-turners to avoid passing of lay lines
                path = genOpportunistic.getPath();
                break;
            case OPPORTUNIST_RIGHT:
                genOpportunistic = new PathGeneratorOpportunistEuclidian360(this.simulationParameters); // instantiate greedy searcher
                genOpportunistic.setEvaluationParameters(false); // use maxTurnTimes of 1-turners to avoid passing of lay lines
                path = genOpportunistic.getPath();
                break;
            }
        } catch (SparseSimulationDataException e) {
            LOGGER.warning(e.getMessage() + "(PathType: " + pathType.getTxtId() + ")");
            List<TimedPositionWithSpeed> points = new ArrayList<TimedPositionWithSpeed>();
            points.add(new TimedPositionWithSpeedImpl(this.simulationParameters.getWindField().getStartTime(), this.simulationParameters.getCourse().get(0), null));
            path = new PathImpl(points, null, 0, true, false);
        }
        return path;
    }

    @Override
    public Path getRaceCourse() {
        return this.raceCourse;
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

        for (RaceProperties raceInfo : ConfigurationManager.INSTANCE.getRacesInfo()) {
            racesNames.add(raceInfo.getName());
        }

        return racesNames;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getCompetitorsNames(int selectedRaceIndex) {

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

    @Override
    public Path getLeg(int selectedCompetitorIndex, int selectedLegIndex) {

        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
        return this.pathGenerator.getPath();
    }

    private void initializePathGenerator(SimulationParameters parameters, int selectedRaceIndex, int selectedLegIndex, int selectedCompetitorIndex) {

        this.pathGenerator = new PathGeneratorTracTrac(parameters);
        //this.pathGenerator.setEvaluationParameters(ConfigurationManager.INSTANCE.getRaceURL(selectedRaceIndex), LIVE_URI, STORED_URI, WIND_SCALE);
        this.pathGenerator.setSelectionParameters(selectedLegIndex, selectedCompetitorIndex);
    }

    /*private Path getFromResourcesOrDownload(String pathName, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) throws Exception {

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
            SimulatorUtils.saveToFile(path, SimulatorUtils.getPathPrefix() + "\\resources\\" + fileName);
        }

        return path;
    }*/
}

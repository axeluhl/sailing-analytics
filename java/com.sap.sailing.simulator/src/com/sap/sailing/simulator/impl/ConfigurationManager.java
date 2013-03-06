package com.sap.sailing.simulator.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Quadruple;

public enum ConfigurationManager {

    INSTANCE;

    private static final String ENVIRONMENT_VARIABLE_NAME = "STG_CONFIG";
    private static final String ENVIRONMENT_RACES_VARIABLE_NAME = "STG_CONFIG";

    private static final String CONFIG_FILE_LOCATION = "resources/STG_configuration.csv";
    private static final String RACES_FILE_LOCATION = "resources/races.csv";

    private final List<Quadruple<String, Double, String, Integer>> _boatClassesInfo = new ArrayList<Quadruple<String, Double, String, Integer>>();
    private final List<Quadruple<String, String, String, Integer>> _racesInfo = new ArrayList<Quadruple<String, String, String, Integer>>();

    private ReadingConfigurationFileStatus status = ReadingConfigurationFileStatus.SUCCESS;
    private String errorMessage = "";

    private ConfigurationManager() {

        this.initFromResources(ConfigurationManager.ENVIRONMENT_VARIABLE_NAME, ConfigurationManager.CONFIG_FILE_LOCATION, true);

        this.initFromResources(ConfigurationManager.ENVIRONMENT_RACES_VARIABLE_NAME, ConfigurationManager.RACES_FILE_LOCATION, false);
    }

    private void initFromResources(String envKeyName, String fileLocation, boolean polarDiagramConfig) {
        String configFileLocation = System.getenv(envKeyName);

        InputStream inputStream = null;
        try {
            if (configFileLocation == null || configFileLocation == "") {
                configFileLocation = fileLocation;
                inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
                this.status = ReadingConfigurationFileStatus.SUCCESS;
                this.errorMessage = "";
            } else if (new File(configFileLocation).exists()) {
                final URL csvFileURL = new URL("file:///" + configFileLocation);
                inputStream = csvFileURL.openStream();
                this.status = ReadingConfigurationFileStatus.SUCCESS;
                this.errorMessage = "";
            } else {
                configFileLocation = fileLocation;
                inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
                this.status = ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE;
                this.errorMessage = "Invalid configuration file path ( " + configFileLocation + ")! Using default configuration values!";
            }

            final InputStreamReader reader = new InputStreamReader(inputStream);
            final BufferedReader buffer = new BufferedReader(reader);
            String line = null;
            String[] elements = null;
            int index = 0;

            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }

                elements = line.split(",");

                if (polarDiagramConfig) {
                    this._boatClassesInfo
                    .add(new Quadruple<String, Double, String, Integer>(elements[0], Double.parseDouble(elements[1]), elements[2], index++));
                } else {
                    this._racesInfo.add(new Quadruple<String, String, String, Integer>(elements[0], elements[1], elements[2], index++));
                }
            }

            buffer.close();
            reader.close();
            inputStream.close();
        } catch (final IOException exception) {
            this.status = ReadingConfigurationFileStatus.IO_ERROR;
            this.errorMessage = "An IO error occured when parsing the configuration file ( " + fileLocation
                    + ")! The original error message is " + exception.getMessage();
        }
    }

    public List<Quadruple<String, Double, String, Integer>> getBoatClassesInfo() {
        return this._boatClassesInfo;
    }

    public List<Quadruple<String, String, String, Integer>> getRacesInfo() {
        return this._racesInfo;
    }

    public int getRacesInfoCount() {
        return this._racesInfo.size();
    }

    public int getBoatClassesInfoCount() {
        return this._boatClassesInfo.size();
    }

    public String getPolarDiagramFileLocation(final int index) {
        return this._boatClassesInfo.get(index).getC();
    }

    public ReadingConfigurationFileStatus getStatus() {
        return this.status;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
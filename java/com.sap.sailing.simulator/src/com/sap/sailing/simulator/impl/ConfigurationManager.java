package com.sap.sailing.simulator.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public enum ConfigurationManager {
	
    INSTANCE;

    private final String _environmentVariableName = "STG_CONFIG";
    private final String _defaultConfigFileLocation = "resources/STG_configuration.csv";
    private final ArrayList<Tuple<String, Double, String>> _boatClassesInfo = new ArrayList<Tuple<String, Double, String>>();
    
    private ReadingConfigurationFileStatus status = ReadingConfigurationFileStatus.SUCCESS;
    private String errorMessage = ""; 

    private ConfigurationManager() {
        String configFileLocation = System.getenv(this._environmentVariableName);
        InputStream inputStream = null;
        try {
	        if (configFileLocation == null || configFileLocation == "") {
	        	configFileLocation = this._defaultConfigFileLocation;
	            inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
	            this.status = ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE;
	            this.errorMessage = "Cannot find STG_CONFIG environment variable! Using default configuration values!";
	        } 
	        else if (new File(configFileLocation).exists()) {
                URL csvFileURL = new URL("file:///" + configFileLocation);
                inputStream = csvFileURL.openStream();
	            this.status = ReadingConfigurationFileStatus.SUCCESS;
	            this.errorMessage = "";
            }
            else {
                configFileLocation = this._defaultConfigFileLocation;
                inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
	            this.status = ReadingConfigurationFileStatus.ERROR_FINDING_CONFIG_FILE;
	            this.errorMessage = "Invalid configuration file path ( " + configFileLocation + ")! Using default configuration values!";
            }
            
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader buffer = new BufferedReader(reader);
            String line = null;
            String[] elements = null;
            int index = 0;

            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }

                elements = line.split(",");

                this._boatClassesInfo.add(new Tuple<String, Double, String>(elements[0], Double.parseDouble(elements[1]), elements[2], index++));
            }

            buffer.close();
            reader.close();
            inputStream.close();
        } 
        catch (IOException exception) {
        	this.status = ReadingConfigurationFileStatus.IO_ERROR;
        	this.errorMessage = "An IO error occured when parsing the configuration file ( " + this._defaultConfigFileLocation + ")! The original error message is " + exception.getMessage();
        }
    }

    public ArrayList<Tuple<String, Double, String>> getBoatClassesInfo() {
        return this._boatClassesInfo;
    }

    public int getBoatClassesInfoCount() {
        return this._boatClassesInfo.size();
    }

    public String getPolarDiagramFileLocation(int index) {
        return this._boatClassesInfo.get(index).third;
    }

	public ReadingConfigurationFileStatus getStatus() {
		return this.status;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
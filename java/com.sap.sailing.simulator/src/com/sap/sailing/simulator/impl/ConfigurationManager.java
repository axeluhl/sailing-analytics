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
        
        System.out.println("XXXXX: configFileLocation = " + configFileLocation);
        
        InputStream inputStream = null;
        try {
	        if (configFileLocation == null || configFileLocation == "") {
	        	
	        	System.out.println("XXXXX: case 1");
	        	
//	        	configFileLocation = this._defaultConfigFileLocation;
//	            inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
//	            this.status = ReadingConfigurationFileStatus.ERROR_READING_ENV_VAR_VALUE;
//	            this.errorMessage = "Cannot find STG_CONFIG environment variable! Using default configuration values!";
	            
	        	/*
	        	 * On 2012/10/17 12:50 Christopher Ronnewinkel:
	        	 * Could you bring up the warning only in the case that the environment variable has been set to a non-empty value, 
	        	 * and then using this no csv-files can be found? If the environment variable is not set or set to an empty string 
	        	 * the simulator should start as always with the defaults.
	        	 */
	        	
                configFileLocation = this._defaultConfigFileLocation;
                inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileLocation);
	            this.status = ReadingConfigurationFileStatus.SUCCESS;
	            this.errorMessage = "";	            
	        } 
	        else if (new File(configFileLocation).exists()) {
	        	
	        	System.out.println("XXXXX: case 2");
	        	
                URL csvFileURL = new URL("file:///" + configFileLocation);
                inputStream = csvFileURL.openStream();
	            this.status = ReadingConfigurationFileStatus.SUCCESS;
	            this.errorMessage = "";
            }
            else {
            	
            	System.out.println("XXXXX: case 3");
            	
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
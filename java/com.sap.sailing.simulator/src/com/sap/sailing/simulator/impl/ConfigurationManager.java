package com.sap.sailing.simulator.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ConfigurationManager {
    private static ConfigurationManager INSTANCE = null;

    private final String _environmentVariableName = "STG_CONFIG";
    private final String _defaultConfigFileLocation = "resources/STG_configuration.csv";

    private final ArrayList<Tuple<String, Double, String>> _boatClassesInfo = new ArrayList<Tuple<String, Double, String>>();

    private ConfigurationManager() {
        String configFileLocation = System.getenv(this._environmentVariableName);
        if (configFileLocation == null || configFileLocation == "") {
            configFileLocation = this._defaultConfigFileLocation;
        } else {
            File file = new File(configFileLocation);
            if (file.exists() == false) {
                configFileLocation = this._defaultConfigFileLocation;
            }
        }

        try {
            InputStream inputStream = (this.getClass().getClassLoader()).getResourceAsStream(configFileLocation);
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

                this._boatClassesInfo.add(new Tuple<String, Double, String>(elements[0], Double
                        .parseDouble(elements[1]), elements[2], index++));
            }

            buffer.close();
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigurationManager getDefault() {
        if (INSTANCE == null)
            INSTANCE = new ConfigurationManager();

        return INSTANCE;
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
}

package com.sap.sailing.gwt.home.shared.partials.header;

public interface HeaderConstants {
    /**
     * The default URL path pointing to the administration environment; starts with a slash (/) and
     * can be appended to a base URL.
     */
    String ADMIN_CONSOLE_PATH = "/gwt/AdminConsole.html";
    
    /**
     * The name of the tab/window to open the administration UI in
     */
    String ADMIN_CONSOLE_WINDOW = "adminConsoleWindow";
    
    /**
     * The default URL path pointing to the data mining environment; starts with a slash (/) and
     * can be appended to a base URL.
     */
    String DATA_MINING_PATH = "/gwt/DataMining.html";

    /**
     * The name of the tab/window to open the data mining UI in
     */
    String DATA_MINING_WINDOW = "dataMiningWindow";
    
    /**
     * The default URL path pointing to the strategy simulator page; starts with a slash (/) and
     * can be appended to a base URL.
     */
    String STRATEGY_SIMULATOR_PATH = "/gwt/Simulator.html";

    /**
     * The name of the tab/window to open the strategy simulator UI in
     */
    String STRATEGY_SIMULATOR_WINDOW = "strategyDimulatorWindow";
    
    /**
     * Default self target if not specified different.
     */
    String SELF = "_self";
}

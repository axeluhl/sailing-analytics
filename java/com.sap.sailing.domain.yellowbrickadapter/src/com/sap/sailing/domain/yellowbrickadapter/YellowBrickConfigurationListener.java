package com.sap.sailing.domain.yellowbrickadapter;

public interface YellowBrickConfigurationListener {
    void yellowBrickConfigurationAdded(YellowBrickConfiguration configAdded);

    void yellowBrickConfigurationRemoved(YellowBrickConfiguration configRemoved);
    
    void yellowBrickConfigurationUpdated(YellowBrickConfiguration configUpdated);
}

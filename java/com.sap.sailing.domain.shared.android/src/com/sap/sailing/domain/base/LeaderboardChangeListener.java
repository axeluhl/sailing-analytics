package com.sap.sailing.domain.base;

public interface LeaderboardChangeListener {
    void nameChanged(String oldName, String newName);
    void displayNameChanged(String oldDisplayName, String newDisplayName);
}

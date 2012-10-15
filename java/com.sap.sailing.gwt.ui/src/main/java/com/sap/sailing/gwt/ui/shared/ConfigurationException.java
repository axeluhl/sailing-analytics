package com.sap.sailing.gwt.ui.shared;

public class ConfigurationException extends Exception
{
	private static final long	serialVersionUID	= -6821286506978371130L;
	
	public boolean shouldBreak = false;
	
    public ConfigurationException() {
    	this.shouldBreak = false;
    }
    
    public ConfigurationException(String message) {
        super(message);
        this.shouldBreak = false;
    }
    
    public ConfigurationException(String message, boolean shouldBreak) {
        super(message);
        this.shouldBreak = shouldBreak;
    }
    
    public boolean shouldBreak()
    {
    	return this.shouldBreak;
    }
}

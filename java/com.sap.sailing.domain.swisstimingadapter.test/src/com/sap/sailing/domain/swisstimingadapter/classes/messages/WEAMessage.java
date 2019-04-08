package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class WEAMessage {
    private String raceId;
    private String conditions;
    private String temperature;
    private String humidity;
    public WEAMessage(String raceId, String conditions, String temperature, String humidity) {
        super();
        this.raceId = raceId;
        this.conditions = conditions;
        this.temperature = temperature;
        this.humidity = humidity;
    }
    public WEAMessage() {
        super();
    }
    public String getRaceId() {
        return raceId;
    }
    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    public String getTemperature() {
        return temperature;
    }
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    public String getHumidity() {
        return humidity;
    }
    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
    
    public String toString(){
        return "WEA|" + raceId + ";" + conditions + ";" + temperature + ";" +humidity;
    }
    
}

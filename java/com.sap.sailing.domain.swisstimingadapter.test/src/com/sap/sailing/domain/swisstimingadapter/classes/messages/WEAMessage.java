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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((humidity == null) ? 0 : humidity.hashCode());
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WEAMessage other = (WEAMessage) obj;
        if (conditions == null) {
            if (other.conditions != null)
                return false;
        } else if (!conditions.equals(other.conditions))
            return false;
        if (humidity == null) {
            if (other.humidity != null)
                return false;
        } else if (!humidity.equals(other.humidity))
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        if (temperature == null) {
            if (other.temperature != null)
                return false;
        } else if (!temperature.equals(other.temperature))
            return false;
        return true;
    }
    
    
}

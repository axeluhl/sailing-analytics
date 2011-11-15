package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class VERMessage {
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VERMessage(String version) {
        super();
        this.version = version;
    }

    public VERMessage() {
        super();
    }
    
    public String toString(){
        return "VER|" + version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        VERMessage other = (VERMessage) obj;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
    
}

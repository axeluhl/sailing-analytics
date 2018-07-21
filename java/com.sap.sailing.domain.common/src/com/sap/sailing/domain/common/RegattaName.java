package com.sap.sailing.domain.common;


public class RegattaName implements RegattaIdentifier {
    private static final long serialVersionUID = 5975000495693192305L;
    
    private String regattaName;

    RegattaName() {}
    
    public RegattaName(String regattaName) {
        super();
        this.regattaName = regattaName;
    }

    public String getRegattaName() {
        return regattaName;
    }
    
    @Override
    public String toString() {
        return getRegattaName();
    }
    
    @Override
    public Object getRegatta(RegattaFetcher regattaFetcher) {
        return regattaFetcher.getRegatta(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((regattaName == null) ? 0 : regattaName.hashCode());
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
        RegattaName other = (RegattaName) obj;
        if (regattaName == null) {
            if (other.regattaName != null)
                return false;
        } else if (!regattaName.equals(other.regattaName))
            return false;
        return true;
    }
    
}

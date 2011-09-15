package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorDAO extends NamedDAO implements IsSerializable {
    public String twoLetterIsoCountryCode;
    public String threeLetterIocCountryCode;
    public String countryName;

    public CompetitorDAO() {}

    public CompetitorDAO(String name, String twoLetterIsoCountryCode, String threeLetterIocCountryCode, String countryName) {
        super(name);
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
        this.countryName = countryName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((threeLetterIocCountryCode == null) ? 0 : threeLetterIocCountryCode.hashCode());
        result = prime * result + ((twoLetterIsoCountryCode == null) ? 0 : twoLetterIsoCountryCode.hashCode());
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
        CompetitorDAO other = (CompetitorDAO) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (threeLetterIocCountryCode == null) {
            if (other.threeLetterIocCountryCode != null)
                return false;
        } else if (!threeLetterIocCountryCode.equals(other.threeLetterIocCountryCode))
            return false;
        if (twoLetterIsoCountryCode == null) {
            if (other.twoLetterIsoCountryCode != null)
                return false;
        } else if (!twoLetterIsoCountryCode.equals(other.twoLetterIsoCountryCode))
            return false;
        return true;
    }
    
}

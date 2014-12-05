package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.base.Regatta;

public class RegattaAsRegattaLikeIdentifier implements RegattaLikeIdentifier {
    private static final long serialVersionUID = 8770210543912486547L;
    private final String regattaName;
    
    public RegattaAsRegattaLikeIdentifier(Regatta regatta) {
        this.regattaName = regatta.getName();
    }

    @Override
    public String getName() {
        return regattaName;
    }

    @Override
    public void resolve(RegattaLikeIdentifierResolver resolver) {
        resolver.resolveOnRegattaIdentifier(this);
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
        RegattaAsRegattaLikeIdentifier other = (RegattaAsRegattaLikeIdentifier) obj;
        if (regattaName == null) {
            if (other.regattaName != null)
                return false;
        } else if (!regattaName.equals(other.regattaName))
            return false;
        return true;
    }
}

package com.sap.sailing.domain.common.impl;

import java.io.Serializable;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class RacePlaceOrderImpl implements RacePlaceOrder, Serializable {
    private static final long serialVersionUID = 7590835541329816755L;
    
    private Placemark start;
    private Placemark finish;
    private boolean startEqualsFinish;
    
    RacePlaceOrderImpl() {}
    
    public RacePlaceOrderImpl(Placemark startPlace, Placemark finishPlace) {
        this.start = startPlace;
        this.finish = finishPlace;
        startEqualsFinish = this.start.equals(this.finish);
    }

    @Override
    public Placemark getStartPlace() {
        return start;
    }

    @Override
    public Placemark getFinishPlace() {
        return finish;
    }

    @Override
    public String startToString() {
        return finish.getCountryCode() + ", " + finish.getName();
    }

    @Override
    public String finishToString() {
        return finish.getCountryCode() + ", " + finish.getName();
    }

    @Override
    public boolean startEqualsFinish() {
        return startEqualsFinish;
    }
    
    @Override
    public String toString() {
        String result = "";
        if (start != null) {
            result += startToString();
            if (!start.equals(finish)) {
                result += " -> " + finishToString();
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((finish == null) ? 0 : finish.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
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
        RacePlaceOrderImpl other = (RacePlaceOrderImpl) obj;
        if (finish == null) {
            if (other.finish != null)
                return false;
        } else if (!finish.equals(other.finish))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        return true;
    }
}

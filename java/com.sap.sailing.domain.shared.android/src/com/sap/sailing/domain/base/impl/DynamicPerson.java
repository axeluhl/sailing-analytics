package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.NationalityChangeListener;
import com.sap.sailing.domain.base.Person;

public interface DynamicPerson extends Person {
    void setNationality(Nationality newNationality);
    
    void addNationalityChangeListener(NationalityChangeListener listener);
    
    void removeNationalityChangeListener(NationalityChangeListener listener);
}

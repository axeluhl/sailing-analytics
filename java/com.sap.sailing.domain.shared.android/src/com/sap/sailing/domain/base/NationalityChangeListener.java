package com.sap.sailing.domain.base;

import java.io.Serializable;

public interface NationalityChangeListener extends Serializable {
    void nationalityChanged(WithNationality what, Nationality oldNationality, Nationality newNationality);
}

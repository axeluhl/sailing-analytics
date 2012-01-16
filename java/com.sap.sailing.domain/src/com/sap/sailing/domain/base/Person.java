package com.sap.sailing.domain.base;

import java.util.Date;

import com.sap.sailing.domain.common.Named;

public interface Person extends Named, WithImage, WithNationality, WithDescription {
    Date getDateOfBirth();
}

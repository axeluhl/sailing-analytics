package com.sap.sailing.domain.base;

import java.util.Date;

import com.sap.sse.common.Named;

public interface Person extends Named, WithNationality, WithDescription {
    Date getDateOfBirth();
}

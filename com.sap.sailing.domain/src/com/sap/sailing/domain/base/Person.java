package com.sap.sailing.domain.base;

import java.util.Date;

public interface Person extends Named, WithImage, WithNationality, WithDescription {
    Date getDateOfBirth();
}

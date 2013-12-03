package com.sap.sailing.domain.igtimiadapter;

public interface User extends SecurityEntity {
    String getFirstName();

    String getSurname();

    String getEmail();
}

package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.User;

public class UserImpl implements User {
    private final long id;
    private final String firstName;
    private final String surname;
    private final String email;
    
    public UserImpl(long id, String firstName, String surname, String email, boolean blob) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public String toString() {
        return getFirstName()+" "+getSurname()+" <"+getEmail()+">, ID "+getId();
    }

}

package com.sap.sailing.domain.base.impl;

import java.io.InputStream;
import java.util.Date;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class PersonImpl extends NamedImpl implements DynamicPerson {
    private static final long serialVersionUID = -2104903799224233508L;
    private Nationality nationality;
    private final Date dateOfBirth;
    private final String description;
    
    public PersonImpl(String name, Nationality nationality, Date dateOfBirth, String description) {
        super(name);
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.description = description;
    }

    @Override
    public InputStream getImage() {
        throw new UnsupportedOperationException("Image for person not yet implemented");
    }

    @Override
    public Nationality getNationality() {
        return nationality;
    }
    
    @Override
    public void setNationality(Nationality newNationality) {
        this.nationality = newNationality;
    }

    @Override
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String getDescription() {
        return description;
    }

}

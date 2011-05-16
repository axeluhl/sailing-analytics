package com.sap.sailing.domain.base.impl;

import java.io.InputStream;

import com.sap.sailing.domain.base.Nationality;

public class NationalityImpl extends NamedImpl implements Nationality {
    private final String threeLetterAcronym;
    
    public NationalityImpl(String name, String threeLetterAcronym) {
        super(name);
        if (threeLetterAcronym.length() != 3) {
            throw new IllegalArgumentException("Three-letter nationality acronym \""+threeLetterAcronym+"\" doesn't have three letters.");
        }
        this.threeLetterAcronym = threeLetterAcronym;
    }

    @Override
    public InputStream getImage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getThreeLetterAcronym() {
        return threeLetterAcronym;
    }

}

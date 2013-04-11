package com.sap.sailing.odf.resultimport.impl;

import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.ParserFactory;

public class ParserFactoryImpl implements ParserFactory {
    private final DomainFactory swissTimingDomainFactory;
    
    public ParserFactoryImpl(DomainFactory swissTimingDomainFactory) {
        super();
        this.swissTimingDomainFactory = swissTimingDomainFactory;
    }

    @Override
    public OdfBodyParser createOdfBodyParser() {
        return new OdfBodyParserImpl(swissTimingDomainFactory);
    }
}

package com.sap.sailing.odf.resultimport;

import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.odf.resultimport.impl.ParserFactoryImpl;

public interface ParserFactory {
    ParserFactory INSTANCE = new ParserFactoryImpl(DomainFactory.INSTANCE);
    
    OdfBodyParser createOdfBodyParser();
}

package com.sap.sailing.xrr.resultimport;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

public class ParserImpl {
    public RegattaResults parse(InputStream is) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("com.sap.sailing.xrr.resultimport.schema", ParserImpl.class.getClassLoader());
        Unmarshaller um = jc.createUnmarshaller();
        @SuppressWarnings("unchecked")
        RegattaResults o = ((JAXBElement<RegattaResults>) um.unmarshal(is)).getValue();
        return o;
    }
}

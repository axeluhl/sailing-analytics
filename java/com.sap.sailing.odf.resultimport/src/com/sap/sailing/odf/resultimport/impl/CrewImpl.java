package com.sap.sailing.odf.resultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.odf.resultimport.Crew;
import com.sap.sailing.odf.resultimport.Crewmember;
import com.sap.sailing.odf.resultimport.Skipper;

public class CrewImpl extends AbstractNodeWrapper implements Crew {

    public CrewImpl(Node node) {
        super(node);
    }

    @Override
    public Skipper getSkipper() {
        return new SkipperImpl(((Element) getNode()).getElementsByTagName("Skipper").item(0));
    }

    @Override
    public Iterable<Crewmember> getCrewmembers() {
        List<Crewmember> result = new ArrayList<Crewmember>();
        final NodeList crewmembers = ((Element) getNode()).getElementsByTagName("Crewmember");
        for (int i=0; i<crewmembers.getLength(); i++) {
            result.add(new CrewmemberImpl(crewmembers.item(i)));
        }
        return result;
    }

}

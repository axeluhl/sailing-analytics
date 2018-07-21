package com.sap.sailing.domain.tractracadapter.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.tractracadapter.MetadataParser;
import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.common.impl.NamedImpl;
import com.tractrac.model.lib.api.event.IRaceCompetitor;

/**
 * TracTrac objects can be augmented by what TracTrac calls a "DataSheet." These optional data sheets can provide
 * meta data about the object to which they belong. For example, a course's meta data can provide the passing
 * sides for each waypoint in the list. The race can provide a definition of side lines in its meta data. And a
 * control point can define its shape and color.<p>
 * 
 * This class encapsulates the patterns and procedures by which the additional data is extracted from the
 * respective TracTrac data. It can be used for both, clientparams.php or TTCM based data.
 * 
 * @see ClientParamsPHP
 * @see RaceCourseReceiver
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MetadataParserImpl implements MetadataParser {
    private class ControlPointMetaDataImpl extends NamedImpl implements ControlPointMetaData {
        private static final long serialVersionUID = 1L;
        private final MarkType type;
        private final Color color;
        private final String shape;
        private final String pattern;
        private final Serializable id;

        public ControlPointMetaDataImpl(String name, MarkType type, Color color, String shape, String pattern, Serializable id) {
            super(name);
            this.type = type;
            this.color = color;
            this.shape = shape;
            this.pattern = pattern;
            this.id = id;
        }

        @Override
        public MarkType getType() {
            return type;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public String getShape() {
            return shape;
        }

        @Override
        public String getPattern() {
            return pattern;
        }

        @Override
        public Serializable getId() {
            return id;
        }
    }

    public class BoatMetaDataImpl extends NamedImpl implements BoatMetaData  {
        private static final long serialVersionUID = 1L;
        private String id; 
        private UUID uuid; 
        private final String color; 

        public BoatMetaDataImpl(String boatUuid, String boatId, String boatName, String boatColor) {
            super(boatName);
            this.id = boatId;
            this.uuid = null;
            this.color = boatColor;
            if (boatUuid != null) {
                try {
                    this.uuid = UUID.fromString(boatUuid);
                } catch (IllegalArgumentException e) {
                    // fallback, at least id is set to the provided string 
                    if (id == null) {
                        id = boatUuid;
                    }
                }    
            }
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getId() {
            return id;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * Parses the route metadata for additional course information
     * The 'passing side' for each course waypoint is encoded like this...
     * <pre>
     *  Seq.0=LINE
     *  Seq.1=PORT
     *  Seq.2=GATE
     *  Seq.3=STARBOARD
     *  Seq.4=LINE
     * </pre>
     */
    @Override
    public Map<Integer, PassingInstruction> parsePassingInstructionData(String routeMetadataString, Iterable<? extends TracTracControlPoint> controlPoints) {
        Map<Integer, PassingInstruction> result = new HashMap<Integer, PassingInstruction>();
        if (routeMetadataString != null) {
            Map<String, String> routeMetadata = parseMetadata(routeMetadataString);
            int start = routeMetadata.containsKey("Seq." + 0) ? 0 : 1;
            for (int i = start; i < start + Util.size(controlPoints); i++) {
                String seqValue = routeMetadata.get("Seq." + i);
                if (seqValue != null) {
                    final PassingInstruction passingInstructions = PassingInstruction.valueOfIgnoringCase(seqValue);
                    if (passingInstructions != null) {
                        result.put(i - start, passingInstructions);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, String> parseMetadata(String metadata) {
        Map<String, String> metadataMap = new HashMap<String, String>();
        try {
            Properties p = new Properties();
            p.load(new StringReader(metadata));
            metadataMap = new HashMap<String, String>((Map) p);
        } catch (IOException e) {
            // do nothing
        }
        return metadataMap;
    }

    @Override
    public Iterable<ControlPointMetaData> parseControlPointMetadata(TracTracControlPoint controlPoint) {
        final Iterable<ControlPointMetaData> result;
        final String controlPointMetadataString = controlPoint.getMetadata();
        final String controlPointName = controlPoint.getName();
        final Map<String, String> controlPointMetadata;
        if (controlPointMetadataString == null) {
            controlPointMetadata = Collections.emptyMap();
        } else {
            controlPointMetadata = parseMetadata(controlPointMetadataString);
        }
        if (controlPoint.getHasTwoPoints()) {
            // it's a gate
            MarkType type1 = resolveMarkTypeFromMetadata(controlPointMetadata, "P1.Type");
            MarkType type2 = resolveMarkTypeFromMetadata(controlPointMetadata, "P2.Type");
            String color1AsString = controlPointMetadata.get("P1.Color");
            Color color1 = AbstractColor.getCssColor(color1AsString);
            String color2AsString = controlPointMetadata.get("P2.Color");
            Color color2 = AbstractColor.getCssColor(color2AsString);
            String shape1 = controlPointMetadata.get("P1.Shape");
            String shape2 = controlPointMetadata.get("P2.Shape");
            String pattern1 = controlPointMetadata.get("P1.Pattern");
            String pattern2 = controlPointMetadata.get("P2.Pattern");
            UUID mark1UUID = controlPoint.getFirstMarkId();
            UUID mark2UUID = controlPoint.getSecondMarkId();
            String name1 = controlPointMetadata.get("P1.Name");
            if (name1 == null) {
                name1 = controlPointName + " (1)";
            }
            String name2 = controlPointMetadata.get("P2.Name");
            if (name2 == null) {
                name2 = controlPointName + " (2)";
            }
            final Serializable id1 = mark1UUID == null ? controlPoint.getId().toString()+" (1)" : mark1UUID;
            ControlPointMetaData mark1Metadata = new ControlPointMetaDataImpl(name1, type1, color1, shape1, pattern1, id1);
            final Serializable id2 = mark2UUID == null ? controlPoint.getId().toString()+" (2)" : mark2UUID;
            ControlPointMetaData mark2Metadata = new ControlPointMetaDataImpl(name2, type2, color2, shape2, pattern2, id2);
            result = Arrays.asList(new ControlPointMetaData[] { mark1Metadata, mark2Metadata });
        } else {
            MarkType type = resolveMarkTypeFromMetadata(controlPointMetadata, "Type");
            String colorAsString = controlPointMetadata.get("Color");
            Color color = AbstractColor.getCssColor(colorAsString);
            String shape = controlPointMetadata.get("Shape");
            String pattern = controlPointMetadata.get("Pattern");
            ControlPointMetaData markMetadata = new ControlPointMetaDataImpl(controlPointName, type, color, shape, pattern, controlPoint.getId());
            result = Collections.singleton(markMetadata);
        }
        return result;
    }

    private MarkType resolveMarkTypeFromMetadata(Map<String, String> controlPointMetadata, String typePropertyName) {
        MarkType result = MarkType.BUOY;
        String markType = controlPointMetadata.get(typePropertyName);
        if(markType != null && !markType.isEmpty()) {
            for(MarkType m: MarkType.values()) {
                if(m.name().equalsIgnoreCase(markType)) {
                    result = m;
                    break;
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Iterable<TracTracControlPoint>> parseSidelinesFromRaceMetadata(String raceMetadataString,
            Iterable<? extends TracTracControlPoint> controlPoints) {
        Map<String, Iterable<TracTracControlPoint>> result = new HashMap<String, Iterable<TracTracControlPoint>>();
        if (raceMetadataString != null) {
            Map<String, String> sidelineMetadata = parseMetadata(raceMetadataString);
            for (Entry<String, String> entry : sidelineMetadata.entrySet()) {
                if (entry.getKey().startsWith("SIDELINE")) {
                    List<TracTracControlPoint> sidelineCPs = new ArrayList<>();
                    result.put(entry.getKey(), sidelineCPs);
                    for (TracTracControlPoint cp : controlPoints) {
                        String cpName = cp.getName().trim();
                        if (cpName.equals(entry.getValue())) {
                            sidelineCPs.add(cp);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public BoatMetaData parseCompetitorBoat(IRaceCompetitor competitor) {
        BoatMetaData result = null;
        String parsedBoatName = null;
        String parsedBoatId = null;
        String parsedBoatUuid = null;
        String parsedColor = null;
        String raceCompetitorMetadataString = competitor.getMetadata() != null ? competitor.getMetadata().getText() : null;
        if (raceCompetitorMetadataString != null) {
            Map<String, String> competitorMetadata = parseMetadata(raceCompetitorMetadataString);
            for (Entry<String, String> entry : competitorMetadata.entrySet()) {
                if (entry.getKey().equals("boatName")) {
                    parsedBoatName = entry.getValue();
                } else if (entry.getKey().equals("boatId")) {
                    parsedBoatId = entry.getValue();
                } else if (entry.getKey().equals("boatUuid")) {
                    parsedBoatUuid = entry.getValue();
                } else if (entry.getKey().equals("boatColor")) {
                    parsedColor = entry.getValue();
                }
            }
            if (parsedBoatName != null && (parsedBoatId != null || parsedBoatUuid != null)  && parsedColor != null) {
                result = new BoatMetaDataImpl(parsedBoatUuid, parsedBoatId, parsedBoatName, parsedColor);
            }
        }
        return result;
    }
}

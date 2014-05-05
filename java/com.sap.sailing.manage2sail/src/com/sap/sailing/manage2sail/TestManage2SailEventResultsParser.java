package com.sap.sailing.manage2sail;

import java.io.IOException;
import java.io.InputStream;

public class TestManage2SailEventResultsParser {
    
    public static EventResultDescriptor getTestEventResult() throws IOException {
        Manage2SailEventResultsParser parser = new Manage2SailEventResultsParserImpl();
        
        InputStream is = TestManage2SailEventResultsParser.class.getClassLoader().getResourceAsStream("worldcupMallorca2013_Races.json");
        return parser.getEventResult(is);
    }
}

package com.sap.sailing.simulator.test;

import com.sap.sailing.simulator.test.util.TracTracWriter;

public class TestTracTracWriter {

    public static void main(String[] args) throws Exception {

        String[] sources = { "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=5b7a34f6-ec44-11e0-a523-406186cbf87c", };
        TracTracWriter ttWriter = new TracTracWriter(sources);
        ttWriter.write();

    }

}

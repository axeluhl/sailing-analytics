package com.sap.sailing.domain.test.measurements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.impl.Util;

/**
 * A utility for generating output files that contain an artificial test result with a &lt;system-out&gt; tag that has
 * &lt;measurement&gt; tags embedded which list named values for use by the Hudson/Jenkins Measurements Plot plug-in.
 * The output is written by the {@link #writeMeasurementsToFile()} method, but only if a <code>bin/</code> directory
 * exists as subdirectory of the current working directory.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class MeasurementGenerator {
    
    private final String reportFileName;
    private final String testSuiteName;
    private final Set<MeasurementCase> cases;
    
    public MeasurementGenerator(String reportFileName, String testSuiteName) {
        this.reportFileName = reportFileName;
        this.testSuiteName = testSuiteName;
        cases = new HashSet<>();
    }
    
    public MeasurementCase addCase(String name) {
        MeasurementCase result = new MeasurementCase(name);
        cases.add(result);
        return result;
    }
    
    private Iterable<MeasurementCase> getCases() {
        return cases;
    }
    
    public void writeMeasurementsToFile() throws IOException {
        File binDir = new File("./bin");
        if (binDir.exists() && binDir.isDirectory()) {
            Writer w = new BufferedWriter(new FileWriter(new File(binDir, reportFileName)));
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            w.write("<testsuite name=\"" + getSuiteName() + " tests=\"" + getNumberOfTests() + "\" >\n");
            for (MeasurementCase measurementCase : getCases()) {
                w.write("<testcase name=\"" + measurementCase.getName() + "\" status=\"run\" time=\"0\" classname=\""
                        + measurementCase.getName() + "\">\n");
                w.write("<system-out>\n");
                for (Measurement measurement : measurementCase.getMeasurements()) {
                    w.write("&lt;measurement&gt;&lt;name&gt;" + measurement.getName() + "&lt;/name&gt;&lt;value&gt;"
                            + measurement.getValue() + "&lt;/value&gt;&lt;/measurement&gt;");
                }
                w.write("</system-out>\n");
                w.write("</testcase>\n");
            }
            w.write("</testsuite>\n");
            w.close();
        }
    }

    private int getNumberOfTests() {
        return Util.size(getCases());
    }

    private String getSuiteName() {
        return testSuiteName;
    }
}

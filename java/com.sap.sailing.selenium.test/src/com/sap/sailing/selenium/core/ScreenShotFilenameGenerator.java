package com.sap.sailing.selenium.core;

import org.junit.runner.Description;

/**
 * <p>Describes the interface for objects that generate unique file names for screen shots.</p>
 *
 * @author
 *   D049941
 */
public interface ScreenShotFilenameGenerator {
    /**
     * <p>Generates and returns a unique file name for the test case described by the given description.</p>
     *
     * @param description
     *   The test case description.
     * @return
     *   The generated unique file name.
     */
    public String getTargetFilename(Description description);
}

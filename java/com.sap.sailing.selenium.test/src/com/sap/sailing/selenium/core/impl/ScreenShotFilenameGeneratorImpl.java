package com.sap.sailing.selenium.core.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.junit.runner.Description;

import com.sap.sailing.selenium.core.ScreenShotFilenameGenerator;

/**
 * <p></p>
 * 
 * @author
 *   D049941
 */
public class ScreenShotFilenameGeneratorImpl implements ScreenShotFilenameGenerator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS"); //$NON-NLS-1$
        
    private final File directory;

    /**
     * <p></p>
     * 
     * @param directory
     *   
     */
    public ScreenShotFilenameGeneratorImpl(File directory) {
        this.directory = directory;
    }

    /**
     * <p>Generates unique file names for the test case described by the given description.</p>
     * 
     * TODO: Describe the resulting filename
     *
     * @param description
     *   The test case description.
     * @return
     *   The generated unique file name.
     */
    @Override
    public File getTargetFilename(final Description description) {
        final StringBuilder builder = new StringBuilder();
        
        builder.append(description.getMethodName());
        builder.append('-');
        builder.append(DATE_FORMAT.format(new Date()));
        builder.append('-');
        builder.append(UUID.randomUUID().toString());
        builder.append(SCREENSHOT_FILE_EXTENSION);
        
        return new File(new File(this.directory, description.getTestClass().getCanonicalName().replace('.', File.separatorChar)),
                builder.toString());
    }
}

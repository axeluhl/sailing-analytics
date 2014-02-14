package com.sap.sailing.selenium.core.impl;

import java.util.UUID;

import org.junit.runner.Description;

import com.sap.sailing.selenium.core.ScreenShotFilenameGenerator;

public class DefaultFilenameGenerator implements ScreenShotFilenameGenerator {
    @Override
    public String getTargetFilename(Description description) {
        return UUID.randomUUID().toString();
    }

}

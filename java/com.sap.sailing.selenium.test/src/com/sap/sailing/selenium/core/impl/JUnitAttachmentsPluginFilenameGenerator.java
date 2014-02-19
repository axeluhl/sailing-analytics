package com.sap.sailing.selenium.core.impl;

import org.junit.runner.Description;

public class JUnitAttachmentsPluginFilenameGenerator extends DefaultFilenameGenerator {
    @Override
    public String getTargetFilename(Description description) {
        return description.getClassName() + "/" + super.getTargetFilename(description);
    }
}

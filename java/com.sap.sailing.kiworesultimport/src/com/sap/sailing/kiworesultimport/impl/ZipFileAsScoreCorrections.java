package com.sap.sailing.kiworesultimport.impl;

import com.sap.sailing.domain.common.ScoreCorrections;
import com.sap.sailing.kiworesultimport.ZipFile;

public class ZipFileAsScoreCorrections implements ScoreCorrections {
    private final ZipFile zipFile;

    public ZipFileAsScoreCorrections(ZipFile zipFile) {
        super();
        this.zipFile = zipFile;
    }
    
}

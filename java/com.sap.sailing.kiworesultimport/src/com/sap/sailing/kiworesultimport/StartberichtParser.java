package com.sap.sailing.kiworesultimport;

import java.io.InputStream;

public interface StartberichtParser {
    Startbericht parse(InputStream inputStream);
}

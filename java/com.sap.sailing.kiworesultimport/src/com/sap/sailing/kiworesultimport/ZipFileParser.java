package com.sap.sailing.kiworesultimport;

import java.io.InputStream;

public interface ZipFileParser {
    ZipFile parse(InputStream inputStream);
}

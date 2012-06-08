package com.sap.sailing.kiworesultimport;

import java.io.InputStream;

public interface ResultListParser {
    ResultList parse(InputStream inputStream);
}

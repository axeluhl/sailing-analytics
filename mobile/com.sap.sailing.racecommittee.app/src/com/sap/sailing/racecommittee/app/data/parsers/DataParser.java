package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

public interface DataParser<T> {

    T parse(Reader reader) throws Exception;

}

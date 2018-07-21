package com.sap.sailing.domain.swisstimingadapter.classes.services;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions.MessageScriptParsingException;

public interface MessageFileService {
    void writeListToFile(File path, List<Object> msgList) throws IOException;
    List<Object> readListFromFile(File path)  throws MessageScriptParsingException, IOException, ParseException;
}

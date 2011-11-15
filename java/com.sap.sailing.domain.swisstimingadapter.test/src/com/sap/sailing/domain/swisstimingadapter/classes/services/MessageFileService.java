package com.sap.sailing.domain.swisstimingadapter.classes.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions.MessageScriptParsingException;

public interface MessageFileService {
    void writeListToFile(String path, List<Object> msgList) throws IOException;
    List<Object> readListFromFile(String path)  throws MessageScriptParsingException, IOException, ParseException;
}

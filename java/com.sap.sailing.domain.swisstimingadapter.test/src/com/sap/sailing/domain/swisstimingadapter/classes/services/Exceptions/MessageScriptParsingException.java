package com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions;

import java.io.File;

public class MessageScriptParsingException extends Exception {

    /**
     *??? 
     */
    private static final long serialVersionUID = 1L;
    
    private File file;
    private String line;

    public MessageScriptParsingException(String line, File file) {
        super("Parsing Exception while parsing a line from MessageScript");
        this.file = file;
        this.line = line;
    }

    @Override
    public String toString() {
        return "MessageScriptParsingException [file=" + file + ", line=" + line + ", toString()=" + super.toString()
                + "]";
    }

}

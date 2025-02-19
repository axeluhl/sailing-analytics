package com.sap.sse.aicore;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;

import com.sap.sse.aicore.impl.CredentialsParserImpl;

public interface CredentialsParser {
    static CredentialsParser create() {
        return new CredentialsParserImpl();
    }
    
    Credentials parse(Reader r) throws IOException, ParseException;
    
    Credentials parse(CharSequence s) throws ParseException, MalformedURLException;
}

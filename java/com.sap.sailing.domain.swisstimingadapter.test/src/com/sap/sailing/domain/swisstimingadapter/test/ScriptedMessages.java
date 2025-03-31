package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScriptedMessages {

    protected List<String> messages;

    public ScriptedMessages(InputStream scriptInputStream) {
        super();

        messages = new ArrayList<String>();

        try { 
            // read the script from an inputstream
            DataInputStream in = new DataInputStream(scriptInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String strLine;
            
            // read line by line
            while ((strLine = br.readLine()) != null)   {
                messages.add(strLine);
            }
            
            in.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ScriptedMessages(List<String> scriptMessages) {
        super();

        this.messages = scriptMessages;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> scriptMessages) {
        this.messages = scriptMessages;
    }

}

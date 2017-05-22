package com.sap.sailing.domain.swisstimingadapter.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptedMessagesReader {

    protected List<String> messages;

    public ScriptedMessagesReader() {
        messages = new ArrayList<String>();
    }

    public int getMessageCount() {
        return messages.size();
    }

    public boolean addMessagesFromRawMessageFile(InputStream scriptInputStream) {
        Map<String, Integer> messageTypes = new HashMap<String, Integer>();
        try { 
            // read the script from an inputstream
            DataInputStream in = new DataInputStream(scriptInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            char STX = '\u0002';  // ascii for START OF TEXT
            char ETX = '\u0003';  // ascii for END OF TEXT
            
            StreamTokenizer tokenizer = new StreamTokenizer(br);
            tokenizer.ordinaryChar(STX);
            tokenizer.ordinaryChar(ETX);
            
            char[] validChars = new char[] { '|', ':', ';', '+', '-', ' '};
            for(char c: validChars)
                tokenizer.wordChars(c, c);
            
            int token;
            while((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
                
                switch(token) {
                    case StreamTokenizer.TT_WORD:
                        String s = tokenizer.sval;

                        String type = s.substring(0, 3);
                        
                        if(messageTypes.containsKey(type) == false)  {
                            messageTypes.put(type, 1);
                        } else {
                            messageTypes.put(type, messageTypes.get(type) + 1);
                        }
          /*  
                        if("TMD".equals(type))  // just for internal analysis
                        {
                            System.out.println(type + " message with length " + s.length());
                            System.out.println(s);

                            String[] xyz = s.split("\\|");
                            String race = xyz[1];
                            String boat = xyz[2];
                            
                            if(comp.containsKey(boat) == false)
                                comp.put(boat, "STL|"+ race + "|1|" + boat + ";" + boat.substring(0,3) + ";Competitor " + comp.size());
                        }
                        */
                        messages.add(s);

                        break;
                    default:
                }
            }
            
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        /*
        System.out.println(messages.size());
        System.out.println(messageTypes);

        System.out.println(comp.size());
        for(String stl: comp.values())
            System.out.println(stl);
        */
        return true;
    }
    
    public boolean addMessagesFromTextFile(InputStream scriptInputStream) {
    
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
            return false;
        }
        
        return true;
    }

    public void addMessagesFromListScriptedMessagesReader(List<String> scriptMessages) {
        this.messages.addAll(scriptMessages);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> scriptMessages) {
        this.messages = scriptMessages;
    }

}

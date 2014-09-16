package eventimport;

import java.util.ArrayList;

public class EventImport {
    
    public ArrayList<RegattaJSON> getRegattas(String url){ 
        
        EventResults results = new EventParser().parseEvent(url);
        
        return results.getRegattas();
        
    }
    
 
    
}

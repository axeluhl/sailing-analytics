package eventimport;

public class EventImport {
    
    public Iterable<RegattaJSON> getRegattas(String url){ 
        EventResults results = new EventParser().parseEvent(url);
        return results.getRegattas();
    }
}

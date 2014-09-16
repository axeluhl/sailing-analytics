package eventimport;

import java.util.ArrayList;

public class EventResults {
    
    private ArrayList<RegattaJSON> regattas = new ArrayList<RegattaJSON>();
    private String id = "";
    private String name = "";
    private String xrrUrl = "";


    public ArrayList<RegattaJSON> getRegattas() {
        return regattas;
    }

    public void setRegattas(ArrayList<RegattaJSON> regattas) {
        this.regattas = regattas;
    }
    
    public void addRegatta(RegattaJSON regatta){
        this.regattas.add(regatta);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXrrUrl() {
        return xrrUrl;
    }

    public void setXrrUrl(String xrrUrl) {
        this.xrrUrl = xrrUrl;
    }

}

package eventimport;

import java.util.ArrayList;

public class EventResults {
    
    private ArrayList<Regattas> regattas = new ArrayList<Regattas>();
    private String id = "";
    private String name = "";
    private String xrrUrl = "";


    public ArrayList<Regattas> getRegattas() {
        return regattas;
    }

    public void setRegattas(ArrayList<Regattas> regattas) {
        this.regattas = regattas;
    }
    
    public void addRegatta(Regattas regatta){
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

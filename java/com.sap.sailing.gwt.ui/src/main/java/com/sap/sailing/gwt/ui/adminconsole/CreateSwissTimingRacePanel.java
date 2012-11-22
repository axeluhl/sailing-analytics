package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class CreateSwissTimingRacePanel extends FormPanel {
    private TextBox txtbRaceName;
    private TextBox txtbRaceDesc;
    private TextBox txtbCompName;
    private TextBox txtbCompSailNr;
    private MultiWordSuggestOracle suggestionsCompCountr;
    private SuggestBox listbCompCountr;
    private ListBox listbCompetitorList;
    private List<Competitor> competitors;
    private int selectedIndex;
    private int id = 0;
    private final SailingServiceAsync service;
    private ErrorReporter errorReporter;

    public CreateSwissTimingRacePanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter, final StringMessages stringConstants){
        this.service = sailingService;
        this.errorReporter = errorReporter;
        competitors = new ArrayList<CreateSwissTimingRacePanel.Competitor>();
        
        VerticalPanel mainPanel = new VerticalPanel();
        Grid mainGrid = new Grid(3,1);
        mainGrid.setWidth("100%");
        mainPanel.add(mainGrid);
        mainPanel.setSize("100%", "100%");
        this.setWidget(mainPanel);
        
        //Race data panel
        CaptionPanel raceDataPanel = new CaptionPanel("Race");
        raceDataPanel.setSize("90%", "90%");
        mainGrid.setWidget(0, 0, raceDataPanel);
        Grid grid = new Grid(2,2);
        raceDataPanel.add(grid);
        Label lblRaceName = new Label(stringConstants.race() + " " + stringConstants.name() + ":");
        grid.setWidget(0, 0, lblRaceName);
        txtbRaceName = new TextBox();
        grid.setWidget(0, 1, txtbRaceName);
        Label lblRaceDesc = new Label(stringConstants.race() + " " + stringConstants.description()+":");
        grid.setWidget(1, 0, lblRaceDesc);
        txtbRaceDesc = new TextBox();
        grid.setWidget(1, 1, txtbRaceDesc);
        
        //Competitor panel
        CaptionPanel competitorPanel = new CaptionPanel(stringConstants.competitor());
        competitorPanel.setSize("90%", "90%");
        mainGrid.setWidget(1, 0, competitorPanel);
        grid = new Grid(3,3);
        competitorPanel.add(grid);
        
        Label lblCompetitorListTitle = new Label(stringConstants.competitor());
        grid.setWidget(0, 0, lblCompetitorListTitle);
        listbCompetitorList = new ListBox(false);
        listbCompetitorList.setVisibleItemCount(10);
        listbCompetitorList.setWidth("200px");
        listbCompetitorList.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                competitorSelectionChanged();
            }
        });
        grid.setWidget(1, 0, listbCompetitorList);
        
        VerticalPanel compData = new VerticalPanel();
        grid.setWidget(1, 1, compData);
        Grid subGrid = new Grid(3,2);
        compData.add(subGrid);
        Label lblName = new Label(stringConstants.name() + ":");
        subGrid.setWidget(0, 0, lblName);
        txtbCompName = new TextBox();
        txtbCompName.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                competitorChanged();
            }
        });
        subGrid.setWidget(0, 1, txtbCompName);
        Label lblSailNr = new Label(stringConstants.sailNumber()+":");
        subGrid.setWidget(1, 0, lblSailNr);
        txtbCompSailNr = new TextBox();
        txtbCompSailNr.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                competitorChanged();
            }
        });
        subGrid.setWidget(1, 1, txtbCompSailNr);
        Label lblCountr = new Label(stringConstants.country()+":");
        subGrid.setWidget(2, 0, lblCountr);
        suggestionsCompCountr = new MultiWordSuggestOracle();
        service.getCountryCodes(new AsyncCallback<String[]>() {

            @Override
            public void onFailure(Throwable caught) {
                CreateSwissTimingRacePanel.this.errorReporter.reportError(stringConstants.no3LetterCodes());
            }

            @Override
            public void onSuccess(String[] result) {
                for (String cc : result){
                    suggestionsCompCountr.add(cc);
                }
            }
        });
        listbCompCountr = new SuggestBox(suggestionsCompCountr);
        listbCompCountr.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                competitorChanged();
            }
        });
        subGrid.setWidget(2, 1, listbCompCountr);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        grid.setWidget(2, 0, horizontalPanel);
        
        Button bttAddCompetitor = new Button(stringConstants.add());
        bttAddCompetitor.setWidth("100px");
        bttAddCompetitor.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                addCompetitor();
            }
        });
        horizontalPanel.add(bttAddCompetitor);
        
        Button bttDeleteCompetitor = new Button(stringConstants.delete());
        bttDeleteCompetitor.setWidth("100px");
        bttDeleteCompetitor.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                deleteCompetitor();
            }
        });
        horizontalPanel.add(bttDeleteCompetitor);
        
        Button bttAddRace = new Button(stringConstants.add() + " " + stringConstants.race());
        bttAddRace.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                addRace();
            }
        });
        mainGrid.setWidget(2, 0, bttAddRace);
    }
    
    private void competitorSelectionChanged(){
        selectedIndex = listbCompetitorList.getSelectedIndex();
        if (selectedIndex < 0){
            return;
        }
        Competitor c = getCompetitorById(Integer.parseInt(listbCompetitorList.getValue(selectedIndex)));
        if (c != null){
            txtbCompName.setText(c.getName());
            txtbCompSailNr.setText(c.getSailNumber());
            listbCompCountr.setText(c.getCountry());
        }
    }
    
    private void addCompetitor(){
        Competitor c = new Competitor(id++,"Noname","","");
        listbCompetitorList.addItem(c.toString(), ""+c.getId());
        competitors.add(c);
        listbCompetitorList.setSelectedIndex(listbCompetitorList.getItemCount()-1);
        competitorSelectionChanged();
        txtbCompName.setFocus(true);
    }
    
    private void deleteCompetitor(){
        selectedIndex = listbCompetitorList.getSelectedIndex();
        if (selectedIndex < 0){
            return;
        }
        competitors.remove(getCompetitorById(Integer.parseInt(listbCompetitorList.getValue(selectedIndex))));
        listbCompetitorList.removeItem(selectedIndex);
    }
    
    private void competitorChanged(){
        if (selectedIndex < 0){
            return;
        }
        Competitor c = getCompetitorById(Integer.parseInt(listbCompetitorList.getValue(selectedIndex)));
        if (!txtbCompName.getText().equals(c.getName())){
            c.setName(txtbCompName.getText());
            listbCompetitorList.setItemText(selectedIndex, c.toString());
        }
        c.setCountry(listbCompCountr.getText());
        c.setSailNumber(txtbCompSailNr.getText());
    }
    
    private Competitor getCompetitorById(int id){
        for (Competitor c : competitors){
            if (c.getId() == id)
                return c;
        }
        return null;
    }
    
    private void addRace(){
        String racMessage = "RAC|1|" + txtbRaceName.getText()+";" + txtbRaceDesc.getText();
        
        String stlMessage = "STL|" + txtbRaceName.getText()+"|" + competitors.size();
        for (Competitor c : competitors){
            stlMessage += "|" + c.getSailNumber()+";" + c.getCountry() +";" + c.getName();
        }
        
        String ccgMessage = "CCG|" + txtbRaceName.getText() +"|3|0;LeeGate;LG1;LG2|1;Windwards;WW1|2;LeeGate;LG1;LG2";

        service.sendSwissTimingDummyRace(racMessage,stlMessage,ccgMessage, new AsyncCallback<Void>() {
            
            @Override
            public void onSuccess(Void result) {
                Window.alert("Succesfully sended new race.");
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Race with raceID \"" + txtbRaceName.getText() + "\" already exists.");
            }
        });
    }
    
    private class Competitor{
        private String name;
        private String sailNumber;
        private String country;
        private final int id;
        
        
        public final int getId() {
            return id;
        }
        
        public Competitor(int id, String name, String sailNumber, String country) {
            super();
            this.id = id;
            this.name = name;
            this.sailNumber = sailNumber;
            this.country = country;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getSailNumber() {
            return sailNumber;
        }
        public void setSailNumber(String sailNumber) {
            this.sailNumber = sailNumber;
        }
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String toString(){
            return getName();
        }
    }
}

package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.IntegerBox;

public class AdminConsole implements EntryPoint {

    @Override
    public void onModuleLoad() {
        StringConstants stringConstants = GWT.create(StringConstants.class);
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        rootPanel.add(tabPanel, 10, 10);
        tabPanel.setSize("100%", "100%");
        
        FormPanel formPanel = new FormPanel();
        tabPanel.add(formPanel, "TracTrac Events", false);
        formPanel.setSize("90%", "90%");
        tabPanel.selectTab(0);
        
        VerticalPanel verticalPanel = new VerticalPanel();
        formPanel.setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        
        Grid grid = new Grid(7, 2);
        verticalPanel.add(grid);
        verticalPanel.setCellWidth(grid, "100%");
        
        Label lblPredefined = new Label("Tracked Before");
        grid.setWidget(0, 0, lblPredefined);
        
        ListBox comboBox = new ListBox();
        grid.setWidget(1, 0, comboBox);
        comboBox.addItem(stringConstants.kielWeel2011());
        comboBox.addItem(stringConstants.stgAccount());
        
        Button btnAdd = new Button("Track Again");
        grid.setWidget(1, 1, btnAdd);
        btnAdd.setWidth("100%");
        
        Label lblTrackNewEvent = new Label("Track New Event");
        grid.setWidget(2, 0, lblTrackNewEvent);
        
        Grid grid_1 = new Grid(5, 3);
        grid.setWidget(3, 0, grid_1);
        
        Label lblHostname = new Label("Hostname");
        grid_1.setWidget(0, 1, lblHostname);
        
        TextBox txtbxGermanmastertraclivedk = new TextBox();
        txtbxGermanmastertraclivedk.setText("germanmaster.traclive.dk");
        grid_1.setWidget(0, 2, txtbxGermanmastertraclivedk);
        
        Label lblEventName = new Label("Event name");
        grid_1.setWidget(1, 1, lblEventName);
        
        TextBox txtbxEvent = new TextBox();
        txtbxEvent.setText("event_2011...");
        grid_1.setWidget(1, 2, txtbxEvent);
        
        Label lblLivePort = new Label("Port Live Data");
        grid_1.setWidget(2, 1, lblLivePort);
        
        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        grid_1.setWidget(2, 2, horizontalPanel_1);
        
        IntegerBox integerBox = new IntegerBox();
        integerBox.setText("1520");
        horizontalPanel_1.add(integerBox);
        
        Label lblStoredPort = new Label("Port Stored Data");
        horizontalPanel_1.add(lblStoredPort);
        
        IntegerBox integerBox_1 = new IntegerBox();
        integerBox_1.setText("1521");
        horizontalPanel_1.add(integerBox_1);
        
        Label lblJsonUrl = new Label("JSON URL");
        grid_1.setWidget(3, 1, lblJsonUrl);
        
        TextBox txtbxHttpjsonservicephp = new TextBox();
        grid_1.setWidget(3, 2, txtbxHttpjsonservicephp);
        txtbxHttpjsonservicephp.setVisibleLength(80);
        txtbxHttpjsonservicephp.setText("http://...traclive.dk/events/.../jsonservice.php");
        
        Label lblLiveUri = new Label("Live URI");
        grid_1.setWidget(4, 1, lblLiveUri);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        grid_1.setWidget(4, 2, horizontalPanel);
        
        TextBox txtbxTcp = new TextBox();
        txtbxTcp.setVisibleLength(30);
        txtbxTcp.setText("tcp://...traclive.dk:1520");
        horizontalPanel.add(txtbxTcp);
        
        Label lblStoredUri = new Label("Stored URI");
        horizontalPanel.add(lblStoredUri);
        horizontalPanel.setCellVerticalAlignment(lblStoredUri, HasVerticalAlignment.ALIGN_MIDDLE);
        
        TextBox txtbxTcptraclivedk = new TextBox();
        txtbxTcptraclivedk.setVisibleLength(30);
        txtbxTcptraclivedk.setText("tcp://...traclive.dk:1521");
        horizontalPanel.add(txtbxTcptraclivedk);
        
        Label lblEventsConnectedTo = new Label("Events Currently Tracked");
        grid.setWidget(5, 0, lblEventsConnectedTo);
        
        ListBox listBox = new ListBox(/* multipleSelect */ true);
        grid.setWidget(6, 0, listBox);
        grid.getCellFormatter().setHeight(6, 0, "100%");
        listBox.setWidth("100%");
        listBox.setVisibleItemCount(5);
        
        Button btnRemove = new Button("Remove");
        grid.setWidget(6, 1, btnRemove);
        btnRemove.setWidth("100%");
        grid.getCellFormatter().setVerticalAlignment(6, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
    }
}

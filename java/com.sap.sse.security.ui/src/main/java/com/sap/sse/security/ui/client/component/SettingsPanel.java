package com.sap.sse.security.ui.client.component;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class SettingsPanel extends FlowPanel {

    private UserManagementServiceAsync userManagementService;
    private Map<String, String> settings = null;
    private Map<String, String> settingTypes = null;

    public SettingsPanel(UserManagementServiceAsync userManagementService) {
        super();
        this.userManagementService = userManagementService;
        initComponents();
    }
    
    private void initComponents(){
        clear();
        Label settingsTitle = new Label("Settings");
        add(settingsTitle);
        userManagementService.getSettingTypes(new AsyncCallback<Map<String,String>>() {

            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Map<String, String> result) {
                settingTypes = result;
                updateSettings();
            }
        });
        userManagementService.getSettings(new AsyncCallback<Map<String,String>>() {
            
            @Override
            public void onSuccess(Map<String, String> result) {
                settings = result;
                updateSettings();
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }
    
    private void updateSettings(){
        if (settings == null || settingTypes == null){
            return;
        }
        Grid grid = new Grid(settings.size(), 2);
        grid.addStyleName("settingsPanel-grid");
        int row = 0;
        for (Entry<String, String> e : settingTypes.entrySet()){
            final String key = e.getKey();
            Label keyLabel = new Label(key);
            grid.setWidget(row, 0, keyLabel);
            
            if (e.getValue().equals(Boolean.class.getName())){
                final CheckBox value = new CheckBox();
                value.setValue(Boolean.parseBoolean(settings.get(key)));
                value.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        userManagementService.setSetting(key, Boolean.class.getName(), value.getValue().toString(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                            }

                            @Override
                            public void onSuccess(Void result) {
                            }
                        });
                    }
                });
                grid.setWidget(row, 1, value);
            }
            else if (e.getValue().equals(Integer.class.getName())){
                final IntegerBox value = new IntegerBox();
                value.setValue(Integer.parseInt(settings.get(key)));
                value.addValueChangeHandler(new ValueChangeHandler<Integer>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Integer> event) {
                        userManagementService.setSetting(key, Integer.class.getName(), value.getValue().toString(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                            }

                            @Override
                            public void onSuccess(Void result) {
                            }
                        });
                    }
                });
                grid.setWidget(row, 1, value);
            }
            else {
                final TextBox value = new TextBox();
                value.setText(settings.get(key).toString());
                value.addChangeHandler(new ChangeHandler() {
                    
                    @Override
                    public void onChange(ChangeEvent event) {
                        userManagementService.setSetting(key, String.class.getName(), value.getText(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                            }

                            @Override
                            public void onSuccess(Void result) {
                            }
                        });
                    }
                });
                grid.setWidget(row, 1, value);
            }
            row++;
        }
        add(grid);
    }
}

package com.sap.sse.security.ui.client.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.IconResources;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SettingsPanel extends LayoutPanel {

    private UserManagementServiceAsync userManagementService;
    private Map<String, String> settings = null;
    private Map<String, String> settingTypes = null;
    private Map<String, FlexTable> savedTabs = new HashMap<>();
    private final StringMessages stringMessages;

    public SettingsPanel(UserManagementServiceAsync userManagementService, StringMessages stringMessages) {
        super();
        setHeight("600px"); // TODO this is ugly; it should be 100%, but then the nested tab panels lead to zero height for this panel's tab panel
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        initComponents();
    }

    private void initComponents() {
        clear();
        userManagementService.getSettingTypes(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Map<String, String> result) {
                settingTypes = result;
                updateSettings();
            }
        });
        userManagementService.getSettings(new AsyncCallback<Map<String, String>>() {

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

    private void updateSettings() {
        if (settings == null || settingTypes == null) {
            return;
        }
        TabLayoutPanel tabPanel = new TabLayoutPanel(30, Unit.PX);
        tabPanel.setHeight("95%");
        AbstractEntryPoint.setTabPanelSize(tabPanel, "100%", "95%");

        Map<String, Integer> numberOfSettings = new HashMap<>();
        for (Entry<String, String> e : settingTypes.entrySet()) {
            String[] split = e.getKey().split("_");
            FlexTable flexTable = savedTabs.get(split[0]);
            Integer row = numberOfSettings.get(split[0]);
            if (row == null) {
                row = 0;
            }
            if (flexTable == null) {
                flexTable = new FlexTable();
                savedTabs.put(split[0], flexTable);
                ScrollPanel scrollPanel = new ScrollPanel(flexTable);
                scrollPanel.setHeight("100%");
                scrollPanel.addStyleName("settingsPanel-grid");
                tabPanel.add(scrollPanel, split[0]);
            }

            if (split[0].equals("URLS")) {
                if (!split[1].equals("AUTH")) {
                    createUrlRowEntry(e.getKey(), e.getValue(), flexTable, row);
                }
            } else {
                createSettingRowEntry(e.getKey(), e.getValue(), flexTable, row);
            }
            row++;
            numberOfSettings.put(split[0], row);
        }

        if (savedTabs.get("URLS") != null) {
            FlexTable table = savedTabs.get("URLS");
            int newline = table.getRowCount()+1;
            final TextBox key = new TextBox();
            final TextBox url = new TextBox();
            final TextBox filter = new TextBox();
            table.setWidget(newline, 0, key);
            table.setWidget(newline, 1, url);
            table.setWidget(newline, 2, filter);
            
            Button add = new Button(stringMessages.addURLFilter(), new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    userManagementService.addSetting("URLS_" + key.getText(), String.class.getName(), url.getText(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(caught.getMessage(), NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            Notification.notify("Added url!", NotificationType.SUCCESS);
                        }
                    });
                    userManagementService.addSetting("URLS_AUTH_" + key.getText(), String.class.getName(), filter.getText(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(caught.getMessage(), NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            Notification.notify("Added url filter!", NotificationType.SUCCESS);
                        }
                    });
                }
            });
            table.setWidget(newline+1, 0, add);
        }
        clear();
        add(tabPanel);
    }

    private void createSettingRowEntry(final String key, String sValue, final FlexTable flexTable, final Integer row) {
        Label keyLabel = new Label(key.substring(key.indexOf('_') + 1));
        flexTable.setWidget(row, 0, keyLabel);
        
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final ImageResource statusRedImageResource = IconResources.INSTANCE.statusRed();
        final HTML statusRed =  new HTML(renderer.render(statusRedImageResource));
        statusRed.setTitle("Could not safe property!");
        
        final ImageResource statusGreenImageResource = IconResources.INSTANCE.statusGreen();
        final HTML statusGreen =  new HTML(renderer.render(statusGreenImageResource));
        statusGreen.setTitle("Saved property!");
        
        final ImageResource statusYellowImageResource = IconResources.INSTANCE.statusYellow();
        final HTML statusYellow =  new HTML(renderer.render(statusYellowImageResource));
        statusYellow.setTitle("Trying to safe property...");
        
        flexTable.setWidget(row, 2, statusGreen);
        
        final ImageResource deleteImageResource = com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon();
        HTML delete = new HTML(renderer.render(deleteImageResource));
        delete.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                boolean deleteS = Window.confirm("Are you sure you want to delete this setting?");
                
                if (deleteS){
                    Notification.notify("Not implemented yet", NotificationType.ERROR);
                }
            }
        });
        flexTable.setWidget(row, 3, delete);

        if (sValue.equals(Boolean.class.getName())) {
            final CheckBox value = new CheckBox();
            value.setValue(Boolean.parseBoolean(settings.get(key)));
            value.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    flexTable.setWidget(row, 2, statusYellow);
                    userManagementService.setSetting(key, Boolean.class.getName(), value.getValue().toString(),
                            new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    flexTable.setWidget(row, 2, statusRed);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    flexTable.setWidget(row, 2, statusGreen);
                                }
                            });
                }
            });
            flexTable.setWidget(row, 1, value);
        } else if (sValue.equals(Integer.class.getName())) {
            final IntegerBox value = new IntegerBox();
            value.setValue(Integer.parseInt(settings.get(key)));
            value.addValueChangeHandler(new ValueChangeHandler<Integer>() {

                @Override
                public void onValueChange(ValueChangeEvent<Integer> event) {
                    flexTable.setWidget(row, 2, statusYellow);
                    userManagementService.setSetting(key, Integer.class.getName(), value.getValue().toString(),
                            new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    flexTable.setWidget(row, 2, statusRed);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    flexTable.setWidget(row, 2, statusGreen);
                                }
                            });
                }
            });
            flexTable.setWidget(row, 1, value);
        } else {
            final TextBox value = new TextBox();
            value.setText(settings.get(key));
            value.addChangeHandler(new ChangeHandler() {

                @Override
                public void onChange(ChangeEvent event) {
                    flexTable.setWidget(row, 2, statusYellow);
                    userManagementService.setSetting(key, String.class.getName(), value.getText(),
                            new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    flexTable.setWidget(row, 2, statusRed);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    flexTable.setWidget(row, 2, statusGreen);
                                }
                            });
                }
            });
            flexTable.setWidget(row, 1, value);
        }
    }

    private void createUrlRowEntry(final String key, String sValue, final FlexTable flexTable, final int row) {
        final String labelKey = key.substring(key.indexOf('_') + 1);
        Label keyLabel = new Label(labelKey);
        flexTable.setWidget(row, 0, keyLabel);
        
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final ImageResource statusRedImageResource = IconResources.INSTANCE.statusRed();
        final HTML statusRed =  new HTML(renderer.render(statusRedImageResource));
        statusRed.setTitle("Could not safe property!");
        
        final ImageResource statusGreenImageResource = IconResources.INSTANCE.statusGreen();
        final HTML statusGreen =  new HTML(renderer.render(statusGreenImageResource));
        statusGreen.setTitle("Saved property!");
        
        final ImageResource statusYellowImageResource = IconResources.INSTANCE.statusYellow();
        final HTML statusYellow =  new HTML(renderer.render(statusYellowImageResource));
        statusYellow.setTitle("Trying to safe property...");
        
        flexTable.setWidget(row, 3, statusGreen);
        
        final ImageResource deleteImageResource = com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon();
        HTML delete = new HTML(renderer.render(deleteImageResource));
        delete.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                boolean deleteS = Window.confirm("Are you sure you want to delete this setting?");
                
                if (deleteS){
                    Notification.notify("Not implemented yet", NotificationType.ERROR);
                }
            }
        });
        flexTable.setWidget(row, 4, delete);

        final TextBox value1 = new TextBox();
        value1.setText(settings.get(key));
        value1.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                flexTable.setWidget(row, 3, statusYellow);
                userManagementService.setSetting(key, String.class.getName(), value1.getText(),
                        new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                flexTable.setWidget(row, 3, statusRed);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                flexTable.setWidget(row, 3, statusGreen);
                            }
                        });
            }
        });
        flexTable.setWidget(row, 1, value1);

        final TextBox value2 = new TextBox();
        value2.setText(settings.get("URLS_AUTH_" + labelKey));
        value2.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                flexTable.setWidget(row, 3, statusYellow);
                userManagementService.setSetting("URLS_AUTH_" + labelKey, String.class.getName(), value2.getText(),
                        new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                flexTable.setWidget(row, 3, statusRed);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                flexTable.setWidget(row, 3, statusGreen);
                            }
                        });
            }
        });
        flexTable.setWidget(row, 2, value2);
    }
}

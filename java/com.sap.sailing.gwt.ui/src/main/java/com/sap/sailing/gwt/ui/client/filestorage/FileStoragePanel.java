package com.sap.sailing.gwt.ui.client.filestorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.filestorage.dto.FileStorageServiceDTO;
import com.sap.sse.filestorage.dto.PropertyDTO;
import com.sap.sse.filestorage.dto.PropertyErrors;
import com.sap.sse.gwt.client.ErrorReporter;

public class FileStoragePanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Label activeServiceLabel;
    private final ListBox servicesListBox;
    private CellTable<PropertyDTO> propertiesTable;
    private final Label serviceDescriptionLabel;

    private final List<PropertyDTO> properties = new ArrayList<>();
    private final Map<String, FileStorageServiceDTO> availableServices = new HashMap<>();
    private final Map<PropertyDTO, String> perPropertyErros = new HashMap<>();

    public FileStoragePanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refresh();
            }
        });
        add(refreshButton);
        
        CaptionPanel activeServicePanel = new CaptionPanel(stringMessages.active());
        activeServiceLabel = new Label();
        activeServicePanel.add(activeServiceLabel);
        add(activeServicePanel);
        
        CaptionPanel editServicePanel = new CaptionPanel(stringMessages.edit());
        servicesListBox = new ListBox();
        servicesListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onServiceSelectionChanged();
            }
        });
        editServicePanel.add(servicesListBox);
        
        serviceDescriptionLabel = new Label();
        editServicePanel.add(serviceDescriptionLabel);

        propertiesTable = new CellTable<>();
        ListDataProvider<PropertyDTO> propertiesListDataProvider = new ListDataProvider<>(properties);
        propertiesListDataProvider.addDataDisplay(propertiesTable);

        TextColumn<PropertyDTO> nameColumn = new TextColumn<PropertyDTO>() {
            @Override
            public String getValue(PropertyDTO p) {
                return p.name;
            }
        };
        propertiesTable.addColumn(nameColumn, stringMessages.name());

        Column<PropertyDTO, String> inputColumn = new Column<PropertyDTO, String>(new TextInputCell()) {
            @Override
            public String getValue(PropertyDTO object) {
                return object.value;
            }
        };
        inputColumn.setFieldUpdater(new FieldUpdater<PropertyDTO, String>() {
            @Override
            public void update(int index, PropertyDTO object, String value) {
                object.value = value;
            }
        });
        propertiesTable.addColumn(inputColumn, stringMessages.value());

        TextColumn<PropertyDTO> descriptionColumn = new TextColumn<PropertyDTO>() {
            @Override
            public String getValue(PropertyDTO p) {
                return p.description;
            }
        };
        propertiesTable.addColumn(descriptionColumn, stringMessages.description());

        TextColumn<PropertyDTO> errorColumn = new TextColumn<PropertyDTO>() {
            @Override
            public String getValue(PropertyDTO p) {
                String error = perPropertyErros.get(p);
                return error == null ? "" : error;
            }
        };
        propertiesTable.addColumn(errorColumn, stringMessages.error());
        
        editServicePanel.add(propertiesTable);
        
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        
        Button saveAndTestPropertiesButton = new Button(stringMessages.save());
        saveAndTestPropertiesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveAndTestProperties(null);
            }
        });
        buttonsPanel.add(saveAndTestPropertiesButton);
        
        Button setAsActiveServiceButton = new Button(stringMessages.setAsActive());
        saveAndTestPropertiesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setAsActiveService();
            }
        });
        buttonsPanel.add(setAsActiveServiceButton);
        
        editServicePanel.add(buttonsPanel);
        
        add(editServicePanel);
    }

    private String getSelectedServiceName() {
        return servicesListBox.getItemText(servicesListBox.getSelectedIndex());
    }

    private void saveAndTestProperties(final Callback<Void, Void> callback) {
        Map<String, String> values = new HashMap<String, String>();
        for (PropertyDTO p : properties) {
            values.put(p.name, p.value);
        }
        perPropertyErros.clear();
        sailingService.setFileStorageServiceProperties(getSelectedServiceName(), values, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                sailingService.testFileStorageServiceProperties(getSelectedServiceName(),
                        new AsyncCallback<PropertyErrors>() {
                            @Override
                            public void onSuccess(PropertyErrors result) {
                                if (result != null) {
                                    perPropertyErros.putAll(result.perPropertyMessages);
                                }
                                propertiesTable.redraw();
                                if (callback != null) {
                                    callback.onSuccess(null);
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Could not test properties: " + caught.getMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not set service properties: " + caught.getMessage());
            }
        });
    }

    private void setAsActiveService() {
        saveAndTestProperties(new Callback<Void, Void>() {
            @Override
            public void onSuccess(Void result) {
                sailingService.setActiveFileStorageService(getSelectedServiceName(), new AsyncCallback<PropertyErrors>() {
                    @Override
                    public void onSuccess(PropertyErrors result) {
                        refresh();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not set new active service: " + caught.getMessage());
                    }
                });
            }
            
            @Override
            public void onFailure(Void reason) {
                Window.alert("Cannot activate service with errors");
            }
        });

    }

    private void onServiceSelectionChanged() {
        properties.clear();
        perPropertyErros.clear();
        serviceDescriptionLabel.setText("");
        FileStorageServiceDTO selected = availableServices.get(getSelectedServiceName());
        if (selected == null) {
            return;
        }

        serviceDescriptionLabel.setText(selected.description);
        properties.addAll(Arrays.asList(selected.properties));
    }

    private void refresh() {
        servicesListBox.clear();
        servicesListBox.addItem("");
        availableServices.clear();
        onServiceSelectionChanged();

        sailingService.getActiveFileStorageServiceName(new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                activeServiceLabel.setText(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load active file storage service: " + caught.getMessage());
            }
        });

        sailingService.getAvailableFileStorageServices(new AsyncCallback<FileStorageServiceDTO[]>() {
            @Override
            public void onSuccess(FileStorageServiceDTO[] result) {
                for (FileStorageServiceDTO service : result) {
                    availableServices.put(service.name, service);
                    servicesListBox.addItem(service.name);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load available file storage services: " + caught.getMessage());
            }
        });
    }
}

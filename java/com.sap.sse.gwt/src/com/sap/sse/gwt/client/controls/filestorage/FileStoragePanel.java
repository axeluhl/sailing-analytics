package com.sap.sse.gwt.client.controls.filestorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.controls.TabbingTextInputCell;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtServiceAsync;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

/**
 * Provides a management interface for {@link FileStorageService}s. Allows choosing which service to use, and setting
 * the configuration properties of the available services.
 * 
 * Essentially, this provides a UI for interfacing with the {@link FileStorageManagementService}.
 * 
 * @author Fredrik Teschke
 *
 */
public class FileStoragePanel extends FlowPanel {
    private final StringMessages stringMessages = StringMessages.INSTANCE;
    private final FileStorageManagementGwtServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Label activeServiceLabel;
    private final ListBox servicesListBox;
    private CellTable<FileStorageServicePropertyDTO> propertiesTable;
    private final Label serviceDescriptionLabel;
    private final Label propertiesErrorLabel;

    private final List<FileStorageServicePropertyDTO> properties;
    private final Map<String, FileStorageServiceDTO> availableServices = new HashMap<>();
    private final Map<FileStorageServicePropertyDTO, String> perPropertyErrors = new HashMap<>();
    private final ListDataProvider<FileStorageServicePropertyDTO> propertiesListDataProvider;

    public FileStoragePanel(FileStorageManagementGwtServiceAsync sailingService, ErrorReporter errorReporter) {
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
        VerticalPanel editServicePanelContent = new VerticalPanel();
        editServicePanel.add(editServicePanelContent);
        servicesListBox = new ListBox();
        servicesListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onServiceSelectionChanged();
            }
        });
        editServicePanelContent.add(servicesListBox);
        serviceDescriptionLabel = new Label();
        editServicePanelContent.add(serviceDescriptionLabel);
        propertiesTable = new BaseCelltable<>();
        propertiesTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED); //allow for default tabbing behaviour
        propertiesListDataProvider = new ListDataProvider<>(new ArrayList<FileStorageServicePropertyDTO>());
        properties = propertiesListDataProvider.getList();
        propertiesListDataProvider.addDataDisplay(propertiesTable);
        TextColumn<FileStorageServicePropertyDTO> nameColumn = new TextColumn<FileStorageServicePropertyDTO>() {
            @Override
            public String getValue(FileStorageServicePropertyDTO p) {
                return p.name;
            }
        };
        propertiesTable.addColumn(nameColumn, stringMessages.name());
        Column<FileStorageServicePropertyDTO, String> inputColumn = new Column<FileStorageServicePropertyDTO, String>(
                new TabbingTextInputCell()) {
            @Override
            public String getValue(FileStorageServicePropertyDTO object) {
                return object.value;
            }
        };
        inputColumn.setFieldUpdater(new FieldUpdater<FileStorageServicePropertyDTO, String>() {
            @Override
            public void update(int index, FileStorageServicePropertyDTO object, String value) {
                object.value = value;
            }
        });
        propertiesTable.addColumn(inputColumn, stringMessages.value());
        TextColumn<FileStorageServicePropertyDTO> descriptionColumn = new TextColumn<FileStorageServicePropertyDTO>() {
            @Override
            public String getValue(FileStorageServicePropertyDTO p) {
                return p.description;
            }
        };
        propertiesTable.addColumn(descriptionColumn, stringMessages.description());
        TextColumn<FileStorageServicePropertyDTO> errorColumn = new TextColumn<FileStorageServicePropertyDTO>() {
            @Override
            public String getValue(FileStorageServicePropertyDTO p) {
                String error = perPropertyErrors.get(p);
                return error == null ? "" : error;
            }
        };
        errorColumn.setCellStyleNames("errorLabel");
        propertiesTable.addColumn(errorColumn, stringMessages.error());
        editServicePanelContent.add(propertiesTable);
        propertiesErrorLabel = new Label();
        propertiesErrorLabel.setStyleName("errorLabel");
        editServicePanelContent.add(propertiesErrorLabel);
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
        setAsActiveServiceButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setAsActiveService();
            }
        });
        buttonsPanel.add(setAsActiveServiceButton);
        editServicePanelContent.add(buttonsPanel);
        add(editServicePanel);
        refresh();
    }

    private String getSelectedServiceName() {
        int i = servicesListBox.getSelectedIndex();
        return i < 0 ? "" : servicesListBox.getItemText(i);
    }

    /**
     * Test the properties using {@link FileStorageService#testProperties()}, and display global errors and per-property
     * errors in the according label and table cells.
     * 
     * @param callback
     */
    private void testProperties(final Callback<Void, Void> callback) {
        sailingService.testFileStorageServiceProperties(getSelectedServiceName(), getLocaleInfo(),
                new AsyncCallback<FileStorageServicePropertyErrorsDTO>() {
                    @Override
                    public void onSuccess(FileStorageServicePropertyErrorsDTO result) {
                        perPropertyErrors.clear();
                        propertiesErrorLabel.setText("");
                        if (result != null) {
                            perPropertyErrors.putAll(result.perPropertyMessages);
                            propertiesErrorLabel.setText(result.message);
                        }
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                        propertiesListDataProvider.refresh();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotTestProperties() + ": " + caught.getMessage());
                    }
                });
    }

    private void saveProperties(final Callback<Void, Void> callback) {
        Map<String, String> values = new HashMap<String, String>();
        for (FileStorageServicePropertyDTO p : properties) {
            values.put(p.name, p.value);
        }
        perPropertyErrors.clear();
        sailingService.setFileStorageServiceProperties(getSelectedServiceName(), values, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.couldNotSetProperties() + ": " + caught.getMessage());
            }
        });
    }

    private void saveAndTestProperties(final Callback<Void, Void> callback) {
        saveProperties(new Callback<Void, Void>() {
            @Override
            public void onSuccess(Void result) {
                testProperties(callback);
            }

            @Override
            public void onFailure(Void reason) {
            }
        });
    }

    private void setAsActiveService() {
        saveAndTestProperties(new Callback<Void, Void>() {
            @Override
            public void onSuccess(Void result) {
                sailingService.setActiveFileStorageService(getSelectedServiceName(), getLocaleInfo(), new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        activeServiceLabel.setText(getSelectedServiceName());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotSetActiveService() + ": "
                                + caught.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Void reason) {
            }
        });

    }

    private void onServiceSelectionChanged() {
        properties.clear();
        perPropertyErrors.clear();
        serviceDescriptionLabel.setText("");
        FileStorageServiceDTO selected = availableServices.get(getSelectedServiceName());
        if (selected != null) {
            serviceDescriptionLabel.setText(selected.description);
            properties.addAll(Arrays.asList(selected.properties));
        }
    }
    
    private String getLocaleInfo() {
        return LocaleInfo.getCurrentLocale().getLocaleName();
    }

    private void refresh() {
        String oldSelectedService = getSelectedServiceName();
        servicesListBox.clear();
        servicesListBox.addItem("");
        availableServices.clear();
        propertiesErrorLabel.setText("");
        perPropertyErrors.clear();
        onServiceSelectionChanged();
        sailingService.getActiveFileStorageServiceName(new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                activeServiceLabel.setText(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.couldNotLoadActiveService() + ": " + caught.getMessage());
            }
        });
        sailingService.getAvailableFileStorageServices(getLocaleInfo(), new AsyncCallback<FileStorageServiceDTO[]>() {
            @Override
            public void onSuccess(FileStorageServiceDTO[] result) {
                for (FileStorageServiceDTO service : result) {
                    availableServices.put(service.name, service);
                    servicesListBox.addItem(service.name);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.couldNotLoadAvailableServices() + ": " + caught.getMessage());
            }
        });
        for (int i = 0; i < servicesListBox.getItemCount(); i++) {
            if (servicesListBox.getItemText(i).equals(oldSelectedService)) {
                servicesListBox.setSelectedIndex(i);
            }
        }
        onServiceSelectionChanged();
        testProperties(null);
    }
}

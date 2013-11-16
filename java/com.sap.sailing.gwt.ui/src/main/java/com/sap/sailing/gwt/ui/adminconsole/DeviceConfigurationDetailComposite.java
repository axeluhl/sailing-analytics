package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;

public class DeviceConfigurationDetailComposite extends Composite {
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    private CaptionPanel captionPanel;
    private Grid mainGrid;
    private Button updateButton;
    
    private DeviceConfigurationMatcherDTO matcher;
    private DeviceConfigurationDTO configuration;
    
    private TextBox allowedCourseAreasBox;
    private TextBox mailRecipientBox;
    private TextBox courseNamesBox;
    
    public DeviceConfigurationDetailComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        this.matcher = null;
        this.configuration = null;
        
        captionPanel = new CaptionPanel(stringMessages.configuration());
        VerticalPanel verticalPanel = new VerticalPanel();
        mainGrid = new Grid(4, 2);
        updateButton = new Button(stringMessages.save());
        updateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateConfiguration(sailingService, errorReporter);
            }
        });

        verticalPanel.add(mainGrid);
        verticalPanel.add(updateButton);
        captionPanel.add(verticalPanel);
        initWidget(captionPanel);
    }

    public void setConfiguration(final DeviceConfigurationMatcherDTO matcher) {
        if (matcher == null) {
            clearUi();
        } else {
            sailingService.getDeviceConfiguration(matcher, new AsyncCallback<DeviceConfigurationDTO>() {
                @Override
                public void onSuccess(DeviceConfigurationDTO result) {
                    setupUi(matcher, result);
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                }
            });
        }
    }
    
    private void markAsDrity(boolean dirty) {
        if (dirty) {
            captionPanel.setCaptionText(stringMessages.configuration() + " (CHANGED)");
        } else {
            captionPanel.setCaptionText(stringMessages.configuration());
        }
    }
    
    private void setupUi(DeviceConfigurationMatcherDTO matcher, DeviceConfigurationDTO result) {
        clearUi();
        this.matcher = matcher;
        this.configuration = result;
        
        allowedCourseAreasBox = new TextBox();
        allowedCourseAreasBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (allowedCourseAreasBox.getText().isEmpty()) {
                    configuration.allowedCourseAreaNames = null;
                } else {
                    configuration.allowedCourseAreaNames = Arrays.asList(allowedCourseAreasBox.getText().split(","));
                }
            }
        });
        allowedCourseAreasBox.addKeyUpHandler(dirtyMarker);
        if (configuration.allowedCourseAreaNames != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < configuration.allowedCourseAreaNames.size(); i++) {
                builder.append(configuration.allowedCourseAreaNames.get(i));
                builder.append(',');
            }
            allowedCourseAreasBox.setText(builder.substring(0, builder.length() - 1));
        } else {
            allowedCourseAreasBox.setText("Alpha,Bravo,Charlie");
            markAsDrity(true);
        }
        mainGrid.setWidget(0, 0, new Label(stringMessages.allowedCourseAreas()));
        mainGrid.setWidget(0, 1, allowedCourseAreasBox);
        
        courseNamesBox = new TextBox();
        courseNamesBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (courseNamesBox.getText().isEmpty()) {
                    configuration.byNameDesignerCourseNames = null;
                } else {
                    configuration.byNameDesignerCourseNames = Arrays.asList(courseNamesBox.getText().split(","));
                }
            }
        });
        courseNamesBox.addKeyUpHandler(dirtyMarker);
        if (configuration.byNameDesignerCourseNames != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < configuration.byNameDesignerCourseNames.size(); i++) {
                builder.append(configuration.byNameDesignerCourseNames.get(i));
                builder.append(',');
            }
            courseNamesBox.setText(builder.substring(0, builder.length() - 1));
        } else {
            courseNamesBox.setText("O2,I2");
            markAsDrity(true);
        }
        mainGrid.setWidget(1, 0, new Label(stringMessages.courseNames()));
        mainGrid.setWidget(1, 1, courseNamesBox);
        
        mailRecipientBox = new TextBox();
        mailRecipientBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                configuration.resultsMailRecipient = mailRecipientBox.getText().isEmpty() ? null : mailRecipientBox.getText();
            }
        });
        mailRecipientBox.addKeyUpHandler(dirtyMarker);
        if (result.resultsMailRecipient != null) {
            mailRecipientBox.setText(result.resultsMailRecipient);
        }
        mainGrid.setWidget(3, 0, new Label(stringMessages.resultsMailRecipient()));
        mainGrid.setWidget(3, 1, mailRecipientBox);
    }

    private void clearUi() {
        this.matcher = null;
        this.configuration = null;
        mainGrid.clear();
    }
    
    private void updateConfiguration(final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        if (matcher == null || configuration == null) {
            errorReporter.reportError("Invalid state.");
            return;
        }
        sailingService.createOrUpdateDeviceConfiguration(matcher, configuration, 
                new AsyncCallback<DeviceConfigurationMatcherDTO>() {
            @Override
            public void onSuccess(DeviceConfigurationMatcherDTO matcher) {
                markAsDrity(false);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        });
    }
    
    private KeyUpHandler dirtyMarker = new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            markAsDrity(true);
        }
    };

}

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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
    private ListBox racingProcedureListBox;
    private ListBox designerModeEntryListBox;
    private TextBox courseNamesBox;
    
    public DeviceConfigurationDetailComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        this.matcher = null;
        this.configuration = null;
        
        captionPanel = new CaptionPanel(stringMessages.configuration());
        VerticalPanel verticalPanel = new VerticalPanel();
        mainGrid = new Grid(5, 2);
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
    
    private void setupUi(DeviceConfigurationMatcherDTO matcher, DeviceConfigurationDTO result) {
        clearUi();
        this.matcher = matcher;
        this.configuration = result;
        
        setupCourseAreasBox(0);
        setupRecipientBox(1);
        setupRacingProcedureListBox(2);
        setupCourseDesignerListBox(3);
        setupCourseNameBox(4);
    }

    private void setupRacingProcedureListBox(int gridRow) {
        racingProcedureListBox = new ListBox(false);
        ListBoxUtils.setupRacingProcedureTypeListBox(racingProcedureListBox, configuration.defaultRacingProcedureType, stringMessages);
        racingProcedureListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int index = racingProcedureListBox.getSelectedIndex();
                if (index >= 0) {
                    RacingProcedureType type = RacingProcedureType.valueOf(racingProcedureListBox.getValue(index));
                    configuration.defaultRacingProcedureType = type == RacingProcedureType.UNKNOWN ? null : type;
                } else {
                    configuration.defaultRacingProcedureType = null;
                }
                markAsDrity(true);
            }
        });
        mainGrid.setWidget(gridRow, 0, new Label(stringMessages.racingProcedure()));
        mainGrid.setWidget(gridRow, 1, racingProcedureListBox);
    }

    private void setupCourseDesignerListBox(int gridRow) {
        designerModeEntryListBox = new ListBox(false);
        ListBoxUtils.setupCourseDesignerModeListBox(designerModeEntryListBox, configuration.defaultCourseDesignerMode, stringMessages);
        designerModeEntryListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int index = designerModeEntryListBox.getSelectedIndex();
                if (index >= 0) {
                    CourseDesignerMode mode = CourseDesignerMode.valueOf(designerModeEntryListBox.getValue(index));
                    configuration.defaultCourseDesignerMode = mode == CourseDesignerMode.UNKNOWN ? null : mode;
                } else {
                    configuration.defaultCourseDesignerMode = null;
                }
                markAsDrity(true);
            }
        });
        mainGrid.setWidget(gridRow, 0, new Label(stringMessages.courseDesignerMode()));
        mainGrid.setWidget(gridRow, 1, designerModeEntryListBox);
    }

    private void setupCourseAreasBox(int gridRow) {
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
        mainGrid.setWidget(gridRow, 0, new Label(stringMessages.allowedCourseAreas()));
        mainGrid.setWidget(gridRow, 1, allowedCourseAreasBox);
    }

    private void setupCourseNameBox(int gridRow) {
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
        mainGrid.setWidget(gridRow, 0, new Label(stringMessages.courseNames()));
        mainGrid.setWidget(gridRow, 1, courseNamesBox);
    }

    private void setupRecipientBox(int gridRow) {
        mailRecipientBox = new TextBox();
        mailRecipientBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                configuration.resultsMailRecipient = mailRecipientBox.getText().isEmpty() ? null : mailRecipientBox.getText();
            }
        });
        mailRecipientBox.addKeyUpHandler(dirtyMarker);
        if (configuration.resultsMailRecipient != null) {
            mailRecipientBox.setText(configuration.resultsMailRecipient);
        }
        mainGrid.setWidget(gridRow, 0, new Label(stringMessages.resultsMailRecipient()));
        mainGrid.setWidget(gridRow, 1, mailRecipientBox);
    }

    private void clearUi() {
        this.matcher = null;
        this.configuration = null;
        mainGrid.clear();
    }
    
    private void markAsDrity(boolean dirty) {
        if (dirty) {
            captionPanel.setCaptionText(stringMessages.configuration() + "* (CHANGED)");
        } else {
            captionPanel.setCaptionText(stringMessages.configuration());
        }
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

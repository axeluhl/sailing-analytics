package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class DeviceConfigurationDetailComposite extends Composite {
    
    public interface DeviceConfigurationCloneListener {
        void onCloneRequested(DeviceConfigurationMatcherDTO matcher, DeviceConfigurationDTO configuration);
    }
    
    private static List<String> suggestedCourseNames = Arrays.asList("Upwind", "Downwind");
    
    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;
    
    private final DeviceConfigurationCloneListener cloneListener;
    
    private CaptionPanel captionPanel;
    private VerticalPanel contentPanel;
    private Button cloneButton;
    private Button updateButton;
    
    private DeviceConfigurationMatcherDTO matcher;
    private DeviceConfigurationDTO originalConfiguration;

    protected TextBox identifierBox;
    private ListEditorComposite<String> allowedCourseAreasList;
    private TextBox mailRecipientBox;
    private ListEditorComposite<String> courseNamesList;
    
    private CheckBox overwriteRegattaConfigurationBox;
    private RegattaConfigurationDTO currentRegattaConfiguration;
    
    
    public DeviceConfigurationDetailComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages,
            final DeviceConfigurationCloneListener listener) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        this.cloneListener = listener;
        
        this.matcher = null;
        this.originalConfiguration = null;
        this.currentRegattaConfiguration = null;
        
        captionPanel = new CaptionPanel(stringMessages.configuration());
        VerticalPanel verticalPanel = new VerticalPanel();
        contentPanel = new VerticalPanel();
        cloneButton = new Button(stringMessages.save() + " + " +  "Clone");
        cloneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateConfiguration();
                cloneListener.onCloneRequested(matcher, getResult());
            }
        });
        updateButton = new Button(stringMessages.save());
        updateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateConfiguration();
            }
        });
        verticalPanel.add(contentPanel);
        
        verticalPanel.add(new HTML("<hr  style=\"width:100%;\" />"));
        
        HorizontalPanel actionPanel = new HorizontalPanel();
        actionPanel.add(cloneButton);
        actionPanel.add(updateButton);
        verticalPanel.add(actionPanel);
        
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
        this.originalConfiguration = result;
        this.currentRegattaConfiguration = result.regattaConfiguration;
        
        setupGeneral();
        setupRegattaConfiguration();
    }

    private void setupRegattaConfiguration() {
        Grid grid = new Grid(1, 3);
        
        final Button editButton = new Button(stringMessages.edit());
        overwriteRegattaConfigurationBox = new CheckBox(stringMessages.overwriteRacingProceduresConfiguration());
        overwriteRegattaConfigurationBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                markAsDirty(true);
                editButton.setEnabled(event.getValue());
            }
        });
        grid.setWidget(0, 0, overwriteRegattaConfigurationBox);
        
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new RegattaConfigurationDialog(currentRegattaConfiguration == null ? new RegattaConfigurationDTO() : currentRegattaConfiguration, 
                        stringMessages, new DialogCallback<DeviceConfigurationDTO.RegattaConfigurationDTO>() {
                    @Override
                    public void ok(RegattaConfigurationDTO newConfiguration) {
                        currentRegattaConfiguration = newConfiguration;
                        markAsDirty(true);
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();;
            }
        });
        
        overwriteRegattaConfigurationBox.setValue(currentRegattaConfiguration != null);
        editButton.setEnabled(currentRegattaConfiguration != null);

        Image helpImage = new Image(resources.help());
        helpImage.setAltText(stringMessages.overwriteRacingProceduresConfigurationHelpText());
        helpImage.setTitle(stringMessages.overwriteRacingProceduresConfigurationHelpText());
        
        grid.setWidget(0, 1, editButton);
        grid.setWidget(0, 2, helpImage);
        contentPanel.add(grid);
    }

    private void setupGeneral() {
        Grid grid = new Grid(4, 2);
        int row = 0;
        setupIdentifier(grid, row++);
        setupCourseAreasBox(grid, row++);
        setupRecipientBox(grid, row++);
        setupCourseNameBox(grid, row++);
        
        contentPanel.add(grid);
    }

    protected void setupIdentifier(Grid grid, int gridRow) {
        identifierBox = new TextBox();
        identifierBox.setWidth("80%");
        identifierBox.setText(DeviceConfigurationPanel.renderIdentifiers(matcher.clients));
        identifierBox.setReadOnly(true);
        
        grid.setWidget(gridRow, 0, new Label("Identifier"));
        grid.setWidget(gridRow, 1, identifierBox);
    }

    private void setupCourseAreasBox(Grid grid, int gridRow) {
        List<String> initialValues = originalConfiguration.allowedCourseAreaNames == null ? Collections
                .<String> emptyList() : originalConfiguration.allowedCourseAreaNames;
                
        allowedCourseAreasList = new StringListEditorComposite(initialValues, stringMessages, stringMessages.courseAreas(), resources.removeIcon(),
                SuggestedCourseAreaNames.suggestedCourseAreaNames, stringMessages.enterCourseAreaName());
        allowedCourseAreasList.setWidth("80%");
        allowedCourseAreasList.addValueChangeHandler(dirtyValueMarker);
                
        grid.setWidget(gridRow, 0, new Label(stringMessages.allowedCourseAreas()));
        grid.setWidget(gridRow, 1, allowedCourseAreasList);
    }

    private void setupCourseNameBox(Grid grid, int gridRow) {
        List<String> initialValues = originalConfiguration.byNameDesignerCourseNames == null ? Collections
                .<String> emptyList() : originalConfiguration.byNameDesignerCourseNames;
        
        courseNamesList = new StringListEditorComposite(initialValues, stringMessages, stringMessages.courseNames(), resources.removeIcon(), suggestedCourseNames,
                stringMessages.enterCourseName());
        courseNamesList.setWidth("80%");
        courseNamesList.addValueChangeHandler(dirtyValueMarker);
        grid.setWidget(gridRow, 0, new Label(stringMessages.courseNames()));
        grid.setWidget(gridRow, 1, courseNamesList);
    }

    private void setupRecipientBox(Grid grid, int gridRow) {
        mailRecipientBox = new TextBox();
        mailRecipientBox.setWidth("80%");
        mailRecipientBox.addKeyUpHandler(dirtyMarker);
        if (originalConfiguration.resultsMailRecipient != null) {
            mailRecipientBox.setText(originalConfiguration.resultsMailRecipient);
        }
        grid.setWidget(gridRow, 0, new Label(stringMessages.resultsMailRecipient()));
        grid.setWidget(gridRow, 1, mailRecipientBox);
    }

    private void clearUi() {
        this.matcher = null;
        this.originalConfiguration = null;
        contentPanel.clear();
    }
    
    private void markAsDirty(boolean dirty) {
        if (dirty && !(captionPanel.getTitle() == stringMessages.configuration())) {
            captionPanel.setCaptionText(stringMessages.configuration() + "* (CHANGED)");
        } else {
            captionPanel.setCaptionText(stringMessages.configuration());
        }
    }
    
    private DeviceConfigurationDTO getResult() {
        DeviceConfigurationDTO result = new DeviceConfigurationDTO();
       
        if (!allowedCourseAreasList.getValue().isEmpty()) {
            result.allowedCourseAreaNames = allowedCourseAreasList.getValue();
        }
        
        if (!mailRecipientBox.getText().isEmpty()) {
            result.resultsMailRecipient = mailRecipientBox.getText().isEmpty() ? null : mailRecipientBox.getText();
        }
        
        if (!courseNamesList.getValue().isEmpty()) {
            result.byNameDesignerCourseNames = courseNamesList.getValue();
        }
        
        if (overwriteRegattaConfigurationBox.getValue()) {
            result.regattaConfiguration = currentRegattaConfiguration;
        }
        
        return result;
    }
    
    private void updateConfiguration() {
        if (matcher == null || originalConfiguration == null) {
            errorReporter.reportError("Invalid state.");
            return;
        }
        DeviceConfigurationDTO dto = getResult();
        sailingService.createOrUpdateDeviceConfiguration(matcher, dto, 
                new AsyncCallback<DeviceConfigurationMatcherDTO>() {
            @Override
            public void onSuccess(DeviceConfigurationMatcherDTO matcher) {
                markAsDirty(false);
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
            markAsDirty(true);
        }
    };
    
    private ValueChangeHandler<Iterable<String>> dirtyValueMarker = new ValueChangeHandler<Iterable<String>>() {
        @Override
        public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
            markAsDirty(true);
        }
    };

}

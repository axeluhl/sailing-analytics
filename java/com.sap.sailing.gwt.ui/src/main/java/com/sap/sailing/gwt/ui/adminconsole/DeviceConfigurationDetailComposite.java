package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;

public class DeviceConfigurationDetailComposite extends Composite {

    private final AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;
    private CaptionPanel captionPanel;
    private VerticalPanel contentPanel;
    private Button cloneButton;
    private Button updateButton;
    protected DeviceConfigurationDTO originalConfiguration;
    protected TextBox uuidBox;
    protected TextBox identifierBox;
    private StringListEditorComposite allowedCourseAreasList;
    private TextBox mailRecipientBox;
    private StringListEditorComposite courseNamesList;
    private CheckBox overwriteRegattaConfigurationBox;
    private RegattaConfigurationDTO currentRegattaConfiguration;
    private final UserService userService;

    public static interface DeviceConfigurationFactory {
        void obtainAndSetNameForConfigurationAndAdd(final DeviceConfigurationDTO configurationToObtainAndSetNameForAndAdd);
    }

    public DeviceConfigurationDetailComposite(SailingServiceAsync sailingService, UserService userService,
            ErrorReporter errorReporter, StringMessages stringMessages, final DeviceConfigurationFactory callbackInterface) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.currentRegattaConfiguration = null;
        captionPanel = new CaptionPanel(stringMessages.configuration());
        VerticalPanel verticalPanel = new VerticalPanel();
        contentPanel = new VerticalPanel();
        cloneButton = new Button(stringMessages.save() + " + " + stringMessages.clone());
        cloneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateConfiguration();
                // create a new, cloned configuration object and set a new UUID; then ask for a name,
                // then send it to the server and add to the local list
                final DeviceConfigurationDTO cloned = getResult();
                cloned.id = UUID.randomUUID();
                callbackInterface.obtainAndSetNameForConfigurationAndAdd(cloned);
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
        setConfiguration(null);
        this.userService = userService;
    }

    public void setConfiguration(final DeviceConfigurationWithSecurityDTO config) {
        if (config == null) {
            clearUi();
            setVisible(false);
        } else {
            setupUi(config);
            setVisible(true);
        }
    }

    private void setupUi(DeviceConfigurationWithSecurityDTO config) {
        clearUi();
        this.originalConfiguration = config;
        this.currentRegattaConfiguration = config.regattaConfiguration;
        setupGeneral();
        setupRegattaConfiguration();

        final boolean hasUpdatePermission = userService.hasPermission(config, DefaultActions.UPDATE);
        final boolean hasUpdateAndCreatePermission = hasUpdatePermission
                && userService.hasCreatePermission(SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION);
        cloneButton.setVisible(hasUpdateAndCreatePermission);
        updateButton.setVisible(hasUpdatePermission);
        allowedCourseAreasList.setEnabled(hasUpdatePermission);
        courseNamesList.setEnabled(hasUpdatePermission);
        overwriteRegattaConfigurationBox.setEnabled(hasUpdatePermission);
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
                        }).show();
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
        Grid grid = new Grid(5, 2);
        int row = 0;
        identifierBox = new TextBox();
        identifierBox.setWidth("80%");
        identifierBox.setText(originalConfiguration.name);
        identifierBox.setReadOnly(true);
        grid.setWidget(row, 0, new Label("Identifier"));
        HorizontalPanel panel = new HorizontalPanel();
        final Button qrCodeButton = getQrCodeButton();
        panel.add(identifierBox);
        panel.add(qrCodeButton);
        grid.setWidget(row++, 1, panel);
        uuidBox = new TextBox();
        uuidBox.setWidth("80%");
        uuidBox.setText(originalConfiguration.id.toString());
        uuidBox.setReadOnly(true);
        grid.setWidget(row, 0, new Label(stringMessages.id()));
        grid.setWidget(row++, 1, uuidBox);
        setupCourseAreasBox(grid, row++);
        setupRecipientBox(grid, row++);
        setupCourseNameBox(grid, row++);
        contentPanel.add(grid);
    }

    private void setupCourseAreasBox(Grid grid, int gridRow) {
        List<String> initialValues = originalConfiguration.allowedCourseAreaNames == null ? Collections
                .<String> emptyList() : originalConfiguration.allowedCourseAreaNames;
        allowedCourseAreasList = new StringListEditorComposite(initialValues, stringMessages, stringMessages.courseAreas(), IconResources.INSTANCE.removeIcon(),
                SuggestedCourseAreaNames.suggestedCourseAreaNames, stringMessages.enterCourseAreaName());
        allowedCourseAreasList.setWidth("80%");
        allowedCourseAreasList.addValueChangeHandler(dirtyValueMarker);
        grid.setWidget(gridRow, 0, new Label(stringMessages.allowedCourseAreas()));
        grid.setWidget(gridRow, 1, allowedCourseAreasList);
    }

    private void setupCourseNameBox(Grid grid, int gridRow) {
        List<String> initialValues = originalConfiguration.byNameDesignerCourseNames == null ? Collections
                .<String> emptyList() : originalConfiguration.byNameDesignerCourseNames;
        List<String> suggestedCourseNames = Arrays.asList(stringMessages.upWind(), stringMessages.downWind());
        courseNamesList = new StringListEditorComposite(initialValues, stringMessages, stringMessages.courseNames(), IconResources.INSTANCE.removeIcon(), suggestedCourseNames,
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
        this.originalConfiguration = null;
        contentPanel.clear();
    }

    private void markAsDirty(boolean dirty) {
        if (dirty) {
            if (captionPanel.getCaptionText().equals(stringMessages.configuration())) {
                captionPanel.setCaptionText(stringMessages.configuration() + "* (" + stringMessages.changed() + ")");
            }
        } else {
            if (!captionPanel.getCaptionText().equals(stringMessages.configuration())) {
                captionPanel.setCaptionText(stringMessages.configuration());
            }
        }
    }

    private DeviceConfigurationDTO getResult() {
        DeviceConfigurationDTO result = new DeviceConfigurationDTO();
        result.name = identifierBox.getValue();
        result.id = UUID.fromString(uuidBox.getValue());
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
        if (originalConfiguration == null) {
            errorReporter.reportError(stringMessages.invalidState());
            return;
        }
        DeviceConfigurationDTO dto = getResult();
        sailingService.createOrUpdateDeviceConfiguration(dto, new MarkedAsyncCallback<>(new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                markAsDirty(false);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        }));
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

    private Button getQrCodeButton() {
        Button qrCodeButton = new Button(stringMessages.qrSync());
        qrCodeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (identifierBox.getValue() == null || identifierBox.getValue().isEmpty()) {
                    Notification.notify(stringMessages.thereIsNoIdentifierSet(), NotificationType.ERROR);
                } else {
                    UserDTO currentUser = userService.getCurrentUser();
                    if (currentUser == null) {
                        createAndShowDialogForAccessToken(/* accessToken */ null);
                    } else {
                        userService.getUserManagementService().getOrCreateAccessToken(currentUser.getName(),
                                new MarkedAsyncCallback<>(new AsyncCallback<String>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.couldNotObtainAccessTokenForUser(caught.getMessage()), /* silentMode */ true);
                                    }

                                    @Override
                                    public void onSuccess(String accessToken) {
                                        createAndShowDialogForAccessToken(accessToken);
                                    }
                                }));
                    }
                }
            }
        });
        return qrCodeButton;
    }

    private void createAndShowDialogForAccessToken(String accessToken) {
        final DialogBox dialog = new DeviceConfigurationQRIdentifierDialog(uuidBox.getValue(), identifierBox.getValue(), stringMessages, accessToken);
        dialog.show();
        dialog.center();
    }
}

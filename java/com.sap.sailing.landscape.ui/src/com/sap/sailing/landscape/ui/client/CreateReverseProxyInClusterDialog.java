package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Creates a dialog box for adding a reverse proxy to the cluster.
 * 
 * @author Thomas Stokes
 *
 */
public class CreateReverseProxyInClusterDialog
        extends DataEntryDialog<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO> {

    public static class CreateReverseProxyDTO implements IsSerializable {

        private String instanceType;
        private String name;
        private String region;
        private String availabilityZone;
        private String keyName;

        @Deprecated
        public CreateReverseProxyDTO() { // essential line for GWT serialization
        }

        /**
         * 
         * @param name
         *            The name of the reverse proxy to spawn.
         * @param instanceType
         *            The new instance type.
         * @param availabilityZone
         *            The mixed format of the AZ with the fewest reverse proxies.
         * @param region
         */
        public CreateReverseProxyDTO(String name, String instanceType, String availabilityZone, String region) {
            this.name = name;
            this.instanceType = instanceType;
            this.availabilityZone = availabilityZone;
            this.region = region;

        }

        public String getName() {
            return name;
        }

        public String getInstanceType() {
            return instanceType;
        }

        public String getRegion() {
            return region;
        }

        public String getAvailabilityZone() {
            return availabilityZone;
        }

        public String getKey() {
            return keyName;
        }

        public void setRegion(String region) {
            this.region = region;

        }

        public void setKey(String key) {
            keyName = key;

        }

    }

    private final TextBox proxyName;
    private final ListBox dedicatedInstanceTypeListBox;
    private final ListBox availabilityZone;
    private final String region;
    private final CheckBox useSharedInstance;
    private final ListBox coDeployInstances;
    private final Label nameLabel;
    private final Label instanceTypeLabel;
    private final Label availabilityZoneLabel;
    private Label instancesIdLabel;
    /**
     * A list of all labels in the dialog box.
     */
    private ArrayList<Label> labels;

    /**
     * The dialog box allows users to choose the name, instance type and az.
     *  
     * @param leastpopulatedAzName The az containing the fewest disposable reverse proxies.
     * @param callback This will call after the user selects the "ok" in the box, if they create a reverse proxy.
     */
    public CreateReverseProxyInClusterDialog(StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService, String region, String leastpopulatedAzName,
            DialogCallback<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO> callback) {
        super(stringMessages.reverseProxies(), stringMessages.reverseProxies(), stringMessages.ok(),
                stringMessages.cancel(), new Validator<CreateReverseProxyInClusterDialog.CreateReverseProxyDTO>() {

                    @Override
                    public String getErrorMessage(
                            CreateReverseProxyInClusterDialog.CreateReverseProxyDTO valueToValidate) {
                        if (!Util.hasLength(valueToValidate.getName())) {
                            return stringMessages.pleaseProvideNonEmptyName();
                        } else {
                            return null;
                        }
                    }
                }, callback);
        proxyName = createTextBox("", 20);
        dedicatedInstanceTypeListBox = LandscapeDialogUtil.createInstanceTypeListBox(this, landscapeManagementService,
                stringMessages, SharedLandscapeConstants.DEFAULT_REVERSE_PROXY_INSTANCE_TYPE, errorReporter);
        // Displays the availability zones in the mixed format.
        availabilityZone = LandscapeDialogUtil.createInstanceAZTypeListBox(this, landscapeManagementService,
                stringMessages, leastpopulatedAzName, errorReporter, region);
        this.region = region;
        useSharedInstance = createCheckbox(stringMessages.runOnExisting());
        useSharedInstance.addValueChangeHandler(e -> updateInstanceTypesBasedOnSharedInstanceBox());
        useSharedInstance.setValue(false);
        useSharedInstance.setEnabled(false);
        // setup available instances box, which is initially hidden.
        coDeployInstances = createListBox(false);
        populateCoDeployInstances();
        // setup labels
        nameLabel = new Label(stringMessages.name());
        instanceTypeLabel = new Label(stringMessages.instanceType());
        availabilityZoneLabel = new Label(stringMessages.availabilityZone());
        instancesIdLabel = new Label(stringMessages.instanceId());
        labels = new ArrayList<>(4);
        labels.add(nameLabel);
        labels.add(instanceTypeLabel);
        labels.add(availabilityZoneLabel);
        labels.add(instancesIdLabel);
        instancesIdLabel.setVisible(false);
        coDeployInstances.setVisible(false);
        validateAndUpdate();
    }

    private void populateCoDeployInstances() {
        // TODO Fill with instanceID/name
    }

    private void updateInstanceTypesBasedOnSharedInstanceBox() {
        if (useSharedInstance.getValue()) {
            // box checked
            proxyName.setVisible(false);
            availabilityZone.setVisible(false);
            dedicatedInstanceTypeListBox.setVisible(false);
            labelVisibility(false);
            instancesIdLabel.setVisible(true);
            coDeployInstances.setVisible(true);
        } else {
            // box unchecked
            proxyName.setVisible(true);
            availabilityZone.setVisible(true);
            dedicatedInstanceTypeListBox.setVisible(true);
            labelVisibility(true);
            instancesIdLabel.setVisible(false);
            coDeployInstances.setVisible(false);
        }
    }

    /**
     * Makes all labels in the dialog box, which have been added to {@link labels} visible or invisible.
     * 
     * @param visible
     *            true to make all labels visible and false to hide them.
     */
    private void labelVisibility(boolean visible) {
        for (Label label : labels) {
            label.setVisible(visible);
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        final FormPanel result = new FormPanel();
        final VerticalPanel verticalPanel = new VerticalPanel();
        result.add(verticalPanel);
        verticalPanel.add(nameLabel);
        verticalPanel.add(proxyName);
        verticalPanel.add(instanceTypeLabel);
        verticalPanel.add(dedicatedInstanceTypeListBox);
        verticalPanel.add(availabilityZoneLabel);
        verticalPanel.add(availabilityZone);
        verticalPanel.add(useSharedInstance);
        verticalPanel.add(instancesIdLabel);
        verticalPanel.add(coDeployInstances);
        return result;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return proxyName;
    }

    @Override
    protected CreateReverseProxyInClusterDialog.CreateReverseProxyDTO getResult() {
        return new CreateReverseProxyDTO(proxyName.getText(), dedicatedInstanceTypeListBox.getSelectedValue(),
                availabilityZone.getSelectedItemText(), region);
    }
}

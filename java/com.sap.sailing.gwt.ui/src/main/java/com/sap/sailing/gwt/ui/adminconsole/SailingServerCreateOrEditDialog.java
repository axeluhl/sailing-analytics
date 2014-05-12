package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SailingServerDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SailingServerCreateOrEditDialog extends DataEntryDialog<SailingServerDTO> {
    private StringMessages stringConstants;
    private TextBox nameTextBox;
    private TextBox urlTextBox;
    
    private static class SailingServerValidator implements Validator<SailingServerDTO> {

        private StringMessages stringConstants;
        private ArrayList<SailingServerDTO> existingSailingServers;

        public SailingServerValidator(StringMessages stringConstants, Collection<SailingServerDTO> existingSailingServers) {
            this.stringConstants = stringConstants;
            this.existingSailingServers = new ArrayList<SailingServerDTO>(existingSailingServers);
        }

        @Override
        public String getErrorMessage(SailingServerDTO serverToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = serverToValidate.getName() != null && serverToValidate.getName().length() > 0;
            boolean urlNotEmpty = serverToValidate.getUrl() != null && serverToValidate.getUrl().length() > 0;

            boolean unique = true;
            for (SailingServerDTO event : existingSailingServers) {
                if (event.getName().equals(serverToValidate.getName())) {
                    unique = false;
                    break;
                }
            }

        if (!nameNotEmpty) {
            errorMessage = stringConstants.pleaseEnterAName();
        } else if (!urlNotEmpty) {
            errorMessage = stringConstants.pleaseEnterNonEmptyUrl();
        } else if (!unique) {
            errorMessage = stringConstants.eventWithThisNameAlreadyExists();
        }

            return errorMessage;
        }
    }

    public SailingServerCreateOrEditDialog(ArrayList<SailingServerDTO> existingSailingServers, StringMessages stringConstants,
            DialogCallback<SailingServerDTO> callback) {
    	this(existingSailingServers, null, false, stringConstants, callback);
    }

    public SailingServerCreateOrEditDialog(ArrayList<SailingServerDTO> existingSailingServers, SailingServerDTO serverToEdit, StringMessages stringConstants,
            DialogCallback<SailingServerDTO> callback) {
    	this(existingSailingServers, serverToEdit, true, stringConstants, callback);
    }

    private SailingServerCreateOrEditDialog(ArrayList<SailingServerDTO> existingSailingServers, SailingServerDTO serverToEdit, boolean isEditMode, StringMessages stringConstants,
            DialogCallback<SailingServerDTO> callback) {
        super("Sailing Server", null, stringConstants.ok(), stringConstants.cancel(),
        		new SailingServerValidator(stringConstants, existingSailingServers), callback);
        this.stringConstants = stringConstants;
        
        nameTextBox = createTextBox(isEditMode ? serverToEdit.getName() : null);
        nameTextBox.setVisibleLength(50);
        urlTextBox = createTextBox(isEditMode ? serverToEdit.getUrl() : null);
        urlTextBox.setVisibleLength(100);
        nameTextBox.setEnabled(!isEditMode);
    }

    @Override
    protected SailingServerDTO getResult() {
    	return new SailingServerDTO(nameTextBox.getText(), urlTextBox.getText());
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameTextBox);
        formGrid.setWidget(1, 0, new Label(stringConstants.url() + ":"));
        formGrid.setWidget(1, 1, urlTextBox);

        return panel;
    }

    @Override
    public void show() {
        super.show();
        nameTextBox.setFocus(true);
    }

}

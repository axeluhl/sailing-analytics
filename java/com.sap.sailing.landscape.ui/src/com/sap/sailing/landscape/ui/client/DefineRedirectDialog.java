package com.sap.sailing.landscape.ui.client;

import java.util.UUID;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.EventRedirectDTO;
import com.sap.sailing.landscape.ui.shared.EventSeriesRedirectDTO;
import com.sap.sailing.landscape.ui.shared.HomeRedirectDTO;
import com.sap.sailing.landscape.ui.shared.PlainRedirectDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.RedirectWithIdDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class DefineRedirectDialog extends DataEntryDialog<RedirectDTO> {
    private final String RADIO_BUTTON_GROUP_NAME = "redirect-choice";
    
    private final RadioButton plain;
    private final RadioButton home;
    private final RadioButton event;
    private final TextBox eventIdBox;
    private final RadioButton eventSeries;
    private final TextBox eventSeriesIdBox;
    
    private static class Validator implements DataEntryDialog.Validator<RedirectDTO> {
        final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RedirectDTO valueToValidate) {
            final String error;
            if (valueToValidate instanceof RedirectWithIdDTO && ((RedirectWithIdDTO) valueToValidate).getId() == null) {
                error = stringMessages.pleaseProvideAValidId();
            } else {
                error = null;
            }
            return error;
        }
    }

    public DefineRedirectDialog(SailingApplicationReplicaSetDTO<String> applicationReplicaSetToDefineLandingPageFor,
            StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService,
            DialogCallback<RedirectDTO> dialogCallback) {
        super(stringMessages.defineDefaultRedirect(), /* message */ stringMessages.defineDefaultRedirectMessage(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ new Validator(stringMessages), /* animationEnabled */ true,
                dialogCallback);
        final ValueChangeHandler<Boolean> valueChangeHandler = e->updateIdBoxEnabling();
        plain = new RadioButton(RADIO_BUTTON_GROUP_NAME, stringMessages.redirectPlain());
        plain.addValueChangeHandler(valueChangeHandler);
        home = new RadioButton(RADIO_BUTTON_GROUP_NAME, stringMessages.redirectHome());
        home.addValueChangeHandler(valueChangeHandler);
        event = new RadioButton(RADIO_BUTTON_GROUP_NAME, stringMessages.redirectEvent());
        event.addValueChangeHandler(valueChangeHandler);
        eventIdBox = createTextBox("", 40);
        eventSeries = new RadioButton(RADIO_BUTTON_GROUP_NAME, stringMessages.redirectEventSeries());
        eventSeries.addValueChangeHandler(valueChangeHandler);
        eventSeriesIdBox = createTextBox("", 40);
    }
    
    private void updateIdBoxEnabling() {
        eventIdBox.setEnabled(event.getValue());
        eventSeriesIdBox.setEnabled(eventSeries.getValue());
        validateAndUpdate();
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(4, 2);
        int row = 0;
        result.setWidget(row++, 0, plain);
        result.setWidget(row++, 0, home);
        result.setWidget(row, 0, event);
        result.setWidget(row++, 1, eventIdBox);
        result.setWidget(row, 0, eventSeries);
        result.setWidget(row++, 1, eventSeriesIdBox);
        plain.setValue(true, /* fireEvents */ true);
        return result;
    }

    @Override
    protected RedirectDTO getResult() {
        final RedirectDTO result;
        if (plain.getValue()) {
            result = new PlainRedirectDTO();
        } else if (home.getValue()) {
            result = new HomeRedirectDTO();
        } else if (event.getValue()) {
            UUID id;
            try {
                id = UUID.fromString(eventIdBox.getValue());
            } catch (Exception e) {
                id = null;
            }
            result = new EventRedirectDTO(id);
        } else if (eventSeries.getValue()) {
            UUID id;
            try {
                id = UUID.fromString(eventSeriesIdBox.getValue());
            } catch (Exception e) {
                id = null;
            }
            result = new EventSeriesRedirectDTO(id);
        } else {
            throw new IllegalStateException("Internal error; one of the radio buttons should have been selected");
        }
        return result;
    }
}

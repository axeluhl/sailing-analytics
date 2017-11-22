package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.adminconsole.StringMessages;


public class PairingListPanel extends SimplePanel implements PairingListDisplayer {

    private CaptionPanel eventsPanel;

    public PairingListPanel(final SailingServiceAsync sailingService, StringMessages stringMessages) {
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        this.eventsPanel = new CaptionPanel("Pairing List");
        mainPanel.add(eventsPanel);
        VerticalPanel eventsContentPanel = new VerticalPanel();
        eventsPanel.setContentWidget(eventsContentPanel);
   
        TextBox flightTextBox = new TextBox();
        TextBox groupTextBox = new TextBox();
        TextBox competitorTextBox = new TextBox();

        Label pairingListLabel = new Label();

        Button generatePairingListButton = new Button(stringMessages.ok());
        generatePairingListButton.ensureDebugId("GeneratePairingListButton");
        generatePairingListButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                try {
                    sailingService.calculatePairingList(Integer.parseInt(flightTextBox.getText()), 
                        Integer.parseInt(groupTextBox.getText()), 
                        Integer.parseInt(competitorTextBox.getText()), 
                        new AsyncCallback<PairingListDTO>() {
                            
                            @Override
                            public void onSuccess(PairingListDTO result) {
                                pairingListLabel.setText(Arrays.deepToString(result.getPairingListTemplate()));
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                // error handling if something went wrong
                            }
                        }
                    );
                } catch (NumberFormatException e) {
                    // error handling for invalid input
                }
            }
        });
        eventsContentPanel.add(generatePairingListButton);
    }

    @Override
    public void fillPairingList() {
    }

}

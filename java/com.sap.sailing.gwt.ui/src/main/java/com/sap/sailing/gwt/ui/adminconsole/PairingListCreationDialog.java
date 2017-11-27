package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PairingListCreationDialog extends AbstractPairingListCreationDialog<PairingListTemplateDTO> {
    
    private final PairingListTemplateDTO template;
    
    protected static class PairingListParameterValidator extends AbstractPairingListParameterValidator {
        public PairingListParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }

    public PairingListCreationDialog(RegattaIdentifier regattaIdentifier, final StringMessages stringMessages, 
            PairingListTemplateDTO template, DialogCallback<PairingListTemplateDTO> callback) {
        // TODO stringMessages 
        super(regattaIdentifier, stringMessages.pairingLists(), stringMessages, new PairingListParameterValidator(stringMessages), 
                callback);
        
        this.template = template;
        
        this.ensureDebugId("PairingListCreationDialog");
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        
        /* DATA PANEL */
        
        CaptionPanel dataPanel = new CaptionPanel();
        dataPanel.setCaptionText("Pairing List Data");
        
        Grid formGrid = new Grid(4, 2);
        dataPanel.add(formGrid);
        
        formGrid.setWidget(0, 0, new Label("Number of Flights:"));
        formGrid.setWidget(0, 1, new Label(String.valueOf(this.template.getFlightCount())));
        formGrid.setWidget(1, 0, new Label("Number of Groups:"));
        formGrid.setWidget(1, 1, new Label(String.valueOf(this.template.getGroupCount())));
        formGrid.setWidget(2, 0, new Label("Number of competitors:"));
        formGrid.setWidget(2, 1, new Label(String.valueOf(this.template.getCompetitorCount())));
        formGrid.setWidget(3, 0, new Label("Quality:"));
        formGrid.setWidget(3, 1, new Label(String.valueOf(Math.floor(this.template.getQuality() * 1000) / 1000)));
        
        panel.add(dataPanel);
        
        /* PAIRING LIST TEMPLATE PANEL */
        
        CaptionPanel pairingListTemplatePanel = new CaptionPanel();
        pairingListTemplatePanel.setCaptionText("Pairing List Template");
        
        Grid pairingListGrid = new Grid(this.template.getPairingListTemplate().length, 
                this.template.getPairingListTemplate()[0].length);
        pairingListTemplatePanel.add(pairingListGrid);
                
        for (int groupIndex = 0; groupIndex < this.template.getPairingListTemplate().length; groupIndex++) {
            
            for (int boatIndex = 0; boatIndex < this.template.getPairingListTemplate()[0].length; boatIndex++) {
                pairingListGrid.setWidget(groupIndex, boatIndex, 
                        new Label(String.valueOf(this.template.getPairingListTemplate()[groupIndex][boatIndex])));
                
                pairingListGrid.getCellFormatter().setWidth(groupIndex, boatIndex, "50px");
            }
        }
        
        panel.add(pairingListTemplatePanel);

        
        return panel;
    }
    
    
    
}

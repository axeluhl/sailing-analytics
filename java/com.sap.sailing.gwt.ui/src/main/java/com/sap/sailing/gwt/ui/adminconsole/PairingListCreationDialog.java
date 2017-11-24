package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
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
        final VerticalPanel panel = new VerticalPanel();
        
        Grid formGrid = new Grid(1, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0, 0, new Label("Quality:"));
        formGrid.setWidget(0, 1, new Label(String.valueOf(this.template.getQuality())));
        
        Grid pairingListGrid = new Grid(this.template.getPairingListTemplate().length + 1, 
                this.template.getPairingListTemplate()[0].length + 1);
        panel.add(pairingListGrid);
        
        /*for (int i = 1; i < this.template.getPairingListTemplate()[0].length + 1; i++) {
            Label label = new Label(String.valueOf(i));
            label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
            pairingListGrid.setWidget(0, i, label);

        }*/
        
        for (int groupIndex = 0; groupIndex < this.template.getPairingListTemplate().length; groupIndex++) {
            
            /*Label label = new Label(String.valueOf(groupIndex));
            label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
            pairingListGrid.setWidget(groupIndex, 0, label);*/
            
            for (int boatIndex = 0; boatIndex < this.template.getPairingListTemplate()[0].length; boatIndex++) {
                pairingListGrid.setWidget(groupIndex, boatIndex, 
                        new Label(String.valueOf(this.template.getPairingListTemplate()[groupIndex][boatIndex])));
                
                pairingListGrid.getCellFormatter().setWidth(groupIndex, boatIndex, "50px");

            }
        }
        
                
        return panel;
    }
    
    
    
}

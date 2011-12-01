package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedEvent;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedHandler;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;

public class CompareCompetitorsChartDialog extends DialogBox {
    private Anchor closeAnchor;

    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService, List<CompetitorDAO> competitors, RaceIdentifier[] races, StringConstants stringConstants){
        super(true);
        CompareCompetitorsPanel ccp = new CompareCompetitorsPanel(sailingService, competitors, races, stringConstants, (int) (Window.getClientWidth()-300), (int) (Window.getClientHeight()-200));
        ccp.addDataLoadedHandler(new DataLoadedHandler() {
            
            @Override
            public void onDataLoaded(DataLoadedEvent event) {
                CompareCompetitorsChartDialog.this.setPopupPosition(5, 5);
                //CompareCompetitorsChartDialog.this.setSize((int) (Window.getClientWidth()*0.9) + "px", (int) (Window.getClientHeight() * 0.9) + "px");
                CompareCompetitorsChartDialog.this.show();
            }
        });
        this.add(ccp);

        this.setPopupPosition(5, 5);
        //this.setSize((int) (Window.getClientWidth()*0.9) + "px", (int) (Window.getClientHeight() * 0.9) + "px");
        closeAnchor = new Anchor("x");

        FlexTable captionLayoutTable = new FlexTable();
        captionLayoutTable.setWidth("100%");
        captionLayoutTable.setText(0, 0, stringConstants.compareCompetitors());
        captionLayoutTable.setWidget(0, 1, closeAnchor);
        captionLayoutTable.getCellFormatter().setHorizontalAlignment(0, 1,HasHorizontalAlignment.ALIGN_RIGHT);

        HTML caption = (HTML) getCaption();
        caption.getElement().appendChild(captionLayoutTable.getElement());

        caption.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EventTarget target = event.getNativeEvent().getEventTarget();
                Element targetElement = (Element) target.cast();

                if (targetElement == closeAnchor.getElement()) {
                    closeAnchor.fireEvent(event);
                }
            }
        });
        addCloseHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

    }
    
    public void addCloseHandler(ClickHandler handler) {
        closeAnchor.addClickHandler(handler);
    }
}

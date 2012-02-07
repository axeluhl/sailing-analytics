package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.AbstractChartPanel.DataLoadedEvent;
import com.sap.sailing.gwt.ui.leaderboard.AbstractChartPanel.DataLoadedHandler;

/**
 * A dialog box that holds a {@link MultiChartPanel} and manages a {@link RaceSelectionProvider} of its own.
 * 
 * @author Benjamin Ebling, Axel Uhl (d043530)
 *
 */
public class CompareCompetitorsChartDialog extends DialogBox {
    private Anchor closeAnchor;
    
    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService,
            List<RaceIdentifier> races, final CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            StringMessages stringConstants, ErrorReporter errorReporter) {
        super(false);
        RaceSelectionProvider raceSelectionProvider = new RaceSelectionModel();
        raceSelectionProvider.setAllRaces(races);
        final MultiChartPanel ccp = new MultiChartPanel(sailingService, competitorSelectionProvider, raceSelectionProvider,
                timer, stringConstants, (int) (Window.getClientWidth() - 350),
                (int) (Window.getClientHeight() - 170), errorReporter, /* showRaceSelector */ true);
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
        closeAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                competitorSelectionProvider.removeCompetitorSelectionChangeListener(ccp);
                hide();
            }
        });
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                ccp.resize((int) (Window.getClientWidth() - 250), (int) (Window.getClientHeight() - 90));
            }
        });
    }
}

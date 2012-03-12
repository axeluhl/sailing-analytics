package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Collections;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;
import com.sap.sailing.gwt.ui.shared.components.ComponentToolbar;

/**
 * A dialog box that holds a {@link MultiChartPanel} and manages a {@link RaceSelectionProvider} of its own.
 * 
 * @author Benjamin Ebling, Axel Uhl (d043530)
 *
 */
public class CompareCompetitorsChartDialog extends DialogBox {
    private Anchor closeAnchor;
    
    private final RaceSelectionProvider raceSelectionProvider;
    
    private final MultiChartPanel multiChartPanel;
    
    private final CollapsablePanel collapsablePanel;
    
    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService,
            List<RaceIdentifier> races, final CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            StringMessages stringConstants, ErrorReporter errorReporter) {
        super(false);
        raceSelectionProvider = new RaceSelectionModel();
        raceSelectionProvider.setAllRaces(races);
        
        multiChartPanel = new MultiChartPanel(sailingService, competitorSelectionProvider, raceSelectionProvider,
                timer, stringConstants, errorReporter, false);
        multiChartPanel.setSize("100%", "100%");
        
        FlowPanel contentPanel = new FlowPanel();

        collapsablePanel = new CollapsablePanel("", true);
        collapsablePanel.setSize(Window.getClientWidth() - 250 + "px", "100%");
        collapsablePanel.setOpen(true);
        collapsablePanel.setCollapsingEnabled(false);

        ComponentToolbar<MultiChartSettings> toolbar = new ComponentToolbar<MultiChartSettings>(multiChartPanel, stringConstants);
        toolbar.addSettingsButton();
        collapsablePanel.setHeaderToolbar(toolbar);
        collapsablePanel.setContent(contentPanel);
        Widget componentEntryWidget = multiChartPanel.getEntryWidget();

        createRaceChooserPanel(contentPanel);

        contentPanel.add(componentEntryWidget);
        
        this.add(collapsablePanel);
        this.setPopupPosition(15, 15);
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
                competitorSelectionProvider.removeCompetitorSelectionChangeListener(multiChartPanel);
                hide();
            }
        });
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                multiChartPanel.setSize(Window.getClientWidth() - 250 + "px", "100%");
            }
        });
    }
    
    private void createRaceChooserPanel(Panel parentPanel) {
        HorizontalPanel raceChooserPanel = new HorizontalPanel();
        raceChooserPanel.setSpacing(5);
        boolean first = true;
        for (final RaceIdentifier selectedRace : raceSelectionProvider.getAllRaces()) {
            RadioButton raceSelectionRadioButton = new RadioButton("chooseRace");
            raceSelectionRadioButton.setText(selectedRace.toString());
            raceChooserPanel.add(raceSelectionRadioButton);
            if (first) {
                raceSelectionRadioButton.setValue(true);
                selectRace(selectedRace);
                first = false;
            }
            raceSelectionRadioButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectRace(selectedRace);
                }
            });
        }
        parentPanel.add(raceChooserPanel);
        
    }

    private void selectRace(final RaceIdentifier selectedRace) {
        if(selectedRace != null)
            collapsablePanel.getHeaderTextAccessor().setText(selectedRace.getRaceName());
        else
            collapsablePanel.getHeaderTextAccessor().setText("");

        raceSelectionProvider.setSelection(Collections.singletonList(selectedRace));
    }

}

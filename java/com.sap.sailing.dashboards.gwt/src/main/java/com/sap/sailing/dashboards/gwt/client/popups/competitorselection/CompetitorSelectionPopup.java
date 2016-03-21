package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table.CompetitorTable;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table.CompetitorTableRowSelectionListener;
import com.sap.sailing.dashboards.gwt.client.widgets.ActionPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class CompetitorSelectionPopup extends Composite implements HasWidgets, CompetitorTableRowSelectionListener {

    @UiField(provided = true)
    ActionPanel competitorselectionbackground;

    @UiField
    HTMLPanel competitorselectionpopup;

    @UiField
    HTMLPanel header;

    @UiField(provided = true)
    CompetitorTable competitortable;

    @UiField(provided = true)
    ActionPanel cancelButton;

    @UiField(provided = true)
    ActionPanel okButton;

    private CompetitorDTO currentCompetitorSelected;
    private boolean isVisible;
    private CompetitorSelectionPopupResources competitorSelectionPopupResources = CompetitorSelectionPopupResources.INSTANCE;

    private List<CompetitorSelectionListener> competitorSelectionPopupListener;

    private static CompetitorSelectionPopupUiBinder uiBinder = GWT.create(CompetitorSelectionPopupUiBinder.class);

    interface CompetitorSelectionPopupUiBinder extends UiBinder<Widget, CompetitorSelectionPopup> {
    }

    public CompetitorSelectionPopup() {
        competitorSelectionPopupResources.gss().ensureInjected();
        competitorSelectionPopupListener = new ArrayList<CompetitorSelectionListener>();
        competitortable = new CompetitorTable(this);
        isVisible = false;
        competitorselectionbackground = new ActionPanel(Event.TOUCHEVENTS, Event.ONCLICK);
        cancelButton = new ActionPanel(Event.TOUCHEVENTS, Event.ONCLICK);
        okButton = new ActionPanel(Event.TOUCHEVENTS, Event.ONCLICK);
        addActionListenerToActionPanels();
        initWidget(uiBinder.createAndBindUi(this));
        cancelButton.getElement().setInnerText("CANCEL");
        okButton.getElement().setInnerText("OK");
    }

    public void show(List<CompetitorDTO> competitorList) {
        competitortable.setTableContent(competitorList);
        RootLayoutPanel.get().add(this);
        competitorselectionpopup.addStyleName(competitorSelectionPopupResources.gss().popupshow());
        competitorselectionpopup.removeStyleName(competitorSelectionPopupResources.gss().popuphide());
        isVisible = true;
    }

    public void hide() {
        competitorselectionpopup.addStyleName(competitorSelectionPopupResources.gss().popuphide());
        competitorselectionpopup.removeStyleName(competitorSelectionPopupResources.gss().popupshow());
        RootLayoutPanel.get().remove(this);
        isVisible = false;
    }

    public void addListener(CompetitorSelectionListener o) {
        if (o != null && !competitorSelectionPopupListener.contains(o)) {
            this.competitorSelectionPopupListener.add(o);
        }
    }

    public void removeListener(CompetitorSelectionListener o) {
        this.competitorSelectionPopupListener.remove(o);
    }

    public void notifyListenerAboutOKButtonClickedWithSelectedCompetitorName(CompetitorDTO competitor) {
        for (CompetitorSelectionListener newStartAnalysisListener : competitorSelectionPopupListener) {
            newStartAnalysisListener.didClickOKWithSelectedCompetitor(competitor);
        }
        hide();
    }

    public boolean isShown() {
        return isVisible;
    }

    private void addActionListenerToActionPanels() {
        competitorselectionbackground.addActionPanelListener(new ActionPanel.ActionPanelListener() {

            @Override
            public void eventTriggered() {
                hide();
            }
        });
        cancelButton.addActionPanelListener(new ActionPanel.ActionPanelListener() {

            @Override
            public void eventTriggered() {
                hide();
            }
        });
        okButton.addActionPanelListener(new ActionPanel.ActionPanelListener() {

            @Override
            public void eventTriggered() {
                hide();
                if (currentCompetitorSelected != null) {
                    notifyListenerAboutOKButtonClickedWithSelectedCompetitorName(currentCompetitorSelected);
                }
            }
        });
    }

    @Override
    public void didSelectedRowWithCompetitorName(CompetitorDTO competitor) {
        currentCompetitorSelected = competitor;
    }

    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}

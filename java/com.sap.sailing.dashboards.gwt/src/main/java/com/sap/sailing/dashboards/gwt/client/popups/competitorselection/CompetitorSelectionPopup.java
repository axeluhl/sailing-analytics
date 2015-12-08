package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class CompetitorSelectionPopup extends Composite implements HasWidgets, CompetitorTableRowSelectionListener {

    interface CompetitorSelectionPopupStyle extends CssResource {
        String popupshow();

        String popuphide();

        String buttonshow();
    }

    @UiField
    CompetitorSelectionPopupStyle style;

    @UiField
    HTMLPanel competitorselectionbackground;
    
    @UiField
    VerticalPanel competitorselectionpopup;
    
    @UiField
    HTMLPanel header;

    @UiField(provided = true)
    CompetitorTable competitortable;

    @UiField
    Button button;

    private CompetitorDTO currentCompetitorSelected;
    private boolean isVisible;

    private List<CompetitorSelectionListener> competitorSelectionPopupListener;

    private static CompetitorSelectionPopupUiBinder uiBinder = GWT.create(CompetitorSelectionPopupUiBinder.class);

    interface CompetitorSelectionPopupUiBinder extends UiBinder<Widget, CompetitorSelectionPopup> {
    }

    public CompetitorSelectionPopup() {
        competitorSelectionPopupListener = new ArrayList<CompetitorSelectionListener>();
        competitortable = new CompetitorTable(this);
        isVisible = false;
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void show(List<CompetitorDTO> competitorList) {
        competitortable.setTableContent(competitorList);
        RootLayoutPanel.get().add(this);
        this.button.setText("OK");
        competitorselectionpopup.addStyleName(style.popupshow());
        competitorselectionpopup.removeStyleName(style.popuphide());
        isVisible = true;
    }

    public void hide() {
        competitorselectionpopup.addStyleName(style.popuphide());
        competitorselectionpopup.removeStyleName(style.popupshow());
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
    
    public boolean isShown(){
        return isVisible;
    }

    @UiHandler("button")
    void handleClick(ClickEvent e) {
        hide();
        if (currentCompetitorSelected != null) {
            notifyListenerAboutOKButtonClickedWithSelectedCompetitorName(currentCompetitorSelected);
        }
    }
    
    @Override
    public void didSelectedRowWithCompetitorName(CompetitorDTO competitor) {
        button.getElement().addClassName(style.buttonshow());
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

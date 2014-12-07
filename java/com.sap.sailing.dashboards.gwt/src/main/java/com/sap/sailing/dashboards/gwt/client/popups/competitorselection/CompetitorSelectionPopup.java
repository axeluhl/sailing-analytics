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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class CompetitorSelectionPopup extends Composite implements HasWidgets, CompetitorTableRowSelectionListener {

    interface CompetitorSelectionPopupStyle extends CssResource {
        String popupshow();

        String popuphide();

        String buttonshow();
        
        String blurred();

        String not_blurred();
    }

    @UiField
    CompetitorSelectionPopupStyle style;

    @UiField
    HTMLPanel header;

    @UiField(provided = true)
    CompetitorTable competitortable;

    @UiField
    Button button;

    private String currentCompetitorSelected;
    private boolean isVisible;

    private List<CompetitorSelectionPopupListener> competitorSelectionPopupListener;

    private static CompetitorSelectionPopupUiBinder uiBinder = GWT.create(CompetitorSelectionPopupUiBinder.class);

    interface CompetitorSelectionPopupUiBinder extends UiBinder<Widget, CompetitorSelectionPopup> {
    }

    public CompetitorSelectionPopup() {
        competitorSelectionPopupListener = new ArrayList<CompetitorSelectionPopupListener>();
        competitortable = new CompetitorTable(this);
        isVisible = false;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setCompetitorList(List<String> competitorList){
        competitortable.setTableContent(competitorList);
    }

    public void show() {
        RootPanel.get().add(this);
        this.button.setText("OK");
        this.getElement().addClassName(style.popupshow());
        this.getElement().removeClassName(style.popuphide());
        RootLayoutPanel.get().addStyleName(style.blurred());
        isVisible = true;
    }

    public void hide() {
        this.getElement().addClassName(style.popuphide());
        this.getElement().removeClassName(style.popupshow());
        RootLayoutPanel.get().addStyleName(style.not_blurred());
        RootPanel.get().remove(this);
        isVisible = false;
    }
    
    public void addListener(CompetitorSelectionPopupListener o) {
        if (o != null && !competitorSelectionPopupListener.contains(o)) {
            this.competitorSelectionPopupListener.add(o);
        }
    }

    public void removeListener(CompetitorSelectionPopupListener o) {
        this.competitorSelectionPopupListener.remove(o);
    }

    public void notifyListenerAboutOKButtonClickedWithSelectedCompetitorName(String competitorName) {
        for (CompetitorSelectionPopupListener newStartAnalysisListener : competitorSelectionPopupListener) {
            newStartAnalysisListener.didClickedOKWithCompetitorName(competitorName);
        }
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

    @Override
    public void didSelectedRowWithCompetitorName(String competitorName) {
        // TODO Auto-generated method stub
        button.getElement().addClassName(style.buttonshow());
        currentCompetitorSelected = competitorName;
    }
}

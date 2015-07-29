package com.sap.sailing.gwt.home.desktop.partials.standings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;

public class StandingsList extends Widget implements RefreshableWidget<GetMiniLeaderboardDTO> {
    interface MyUiBinder extends UiBinder<Element, StandingsList> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField DivElement itemContainerUi;
    @UiField DivElement noResultsUi;
    @UiField DivElement scoreInformationUi;

    public StandingsList() {
        StandingsResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void setData(GetMiniLeaderboardDTO data) {
        itemContainerUi.removeAllChildren();
        scoreInformationUi.removeAllChildren();
        getElement().getStyle().clearDisplay();
        
        if(data.getItems().isEmpty()) {
            noResultsUi.getStyle().clearDisplay();
            return;
        }
        noResultsUi.getStyle().setDisplay(Display.NONE);
        
        updateScoreInformation(data);
        
        boolean showRaceCounts = data.hasDifferentRaceCounts();
        for (MiniLeaderboardItemDTO item : data.getItems()) {
            itemContainerUi.appendChild(new StandingsListCompetitor(item, showRaceCounts).getElement());
        }
    }

    private void updateScoreInformation(GetMiniLeaderboardDTO data) {
        if (data.getScoreCorrectionText() != null) {
            DivElement scoreCorrectionText = Document.get().createDivElement();
            scoreCorrectionText.setInnerText(data.getScoreCorrectionText());
            scoreInformationUi.appendChild(scoreCorrectionText);
        }
        if (data.getLastScoreUpdate() != null) {
            DivElement lastUpdateElement = Document.get().createDivElement();
            String lastUpdate = DateAndTimeFormatterUtil.longDateFormatter.render(data.getLastScoreUpdate()) + " "
                    + DateAndTimeFormatterUtil.formatElapsedTime(data.getLastScoreUpdate().getTime());
            lastUpdateElement.setInnerText(StringMessages.INSTANCE.lastScoreUpdate() + ": " + lastUpdate);
            scoreInformationUi.appendChild(lastUpdateElement);
        }
    }
}

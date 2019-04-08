package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors;

import java.util.List;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public interface PreRaceCompetitorsView {
    void startingWith(PreRaceCompetitorsPresenter p, AcceptsOneWidget panel);

    public interface PreRaceCompetitorsPresenter {
    }

    void move();

    void setCompetitors(List<CompetitorWithBoatDTO> data);
}

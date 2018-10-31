package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.partials.dialog.confirm.ConfirmDialogFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/** sailor profile overview entry with one sailor profile on mobile */
public class SailorProfileOverviewEntry extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileOverviewEntry> {
    }

    @UiField
    Button detailsButtonUi;

    @UiField
    DivElement badgesDivUi;
    @UiField
    DivElement competitorsDivUi;
    @UiField
    DivElement boatclassesDivUi;
    @UiField
    SectionHeaderContent sectionTitleUi;
    @UiField
    HTMLPanel contentContainerCompetitorsUi;

    @UiField
    DivElement badgesArea;

    private final UUID uuidRef;

    private final SailingProfileOverviewPresenter presenter;

    public SailorProfileOverviewEntry(SailorProfileDTO entry, SailingProfileOverviewPresenter presenter) {
        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        this.sectionTitleUi.setSectionTitle(entry.getName());

        badgesArea.getStyle().setDisplay(Display.NONE);

        // add badges
        for (BadgeDTO badge : entry.getBadges()) {
            Element elem = DOM.createDiv();
            elem.setInnerText(badge.getName());
            badgesDivUi.appendChild(elem);
        }

        // add competitors
        for (SimpleCompetitorWithIdDTO competitor : entry.getCompetitors()) {
            Element elem = DOM.createDiv();
            elem.setInnerText(competitor.getName());
            competitorsDivUi.appendChild(elem);
        }

        // add boatclasses
        for (BoatClassDTO boatclass : entry.getBoatclasses()) {
            Element elem = DOM.createDiv();
            elem.setInnerSafeHtml(SharedSailorProfileResources.TEMPLATES.buildBoatclassIcon(
                    BoatClassImageResolver.getBoatClassIconResource(boatclass.getName()).getSafeUri().asString()));
            elem.getStyle().setDisplay(Display.INLINE_BLOCK);
            boatclassesDivUi.appendChild(elem);
        }
        this.uuidRef = entry.getKey();
        sectionTitleUi.initCollapsibility(contentContainerCompetitorsUi.getElement(), false);

        final Button removeButton = new Button("X");
        removeButton.addClickHandler(e -> {
            e.preventDefault();
            e.getNativeEvent().preventDefault();
            e.getNativeEvent().stopPropagation();
            ConfirmDialogFactory.showConfirmDialog(StringMessages.INSTANCE.sailorProfileRemoveMessage(),
                    StringMessages.INSTANCE.confirmDeletion(), new DialogCallback<Void>() {
                        @Override
                        public void ok(Void v) {
                            presenter.removeSailorProfile(uuidRef);
                        }

                        @Override
                        public void cancel() {
                        }
                    });
        });

        removeButton.addStyleName(SharedResources.INSTANCE.mainCss().buttonred());
        sectionTitleUi.appendHeaderElement(removeButton);
    }

    @UiHandler("detailsButtonUi")
    void onClick(ClickEvent e) {
        presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(uuidRef));
    }

}

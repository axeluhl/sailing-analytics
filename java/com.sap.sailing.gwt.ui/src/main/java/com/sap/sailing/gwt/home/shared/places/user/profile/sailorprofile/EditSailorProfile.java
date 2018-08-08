package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection.NotificationCallback;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Implementation of {@link SharedSailorProfileView} where users can change their preferred selections and
 * notifications.
 */
public class EditSailorProfile extends Composite implements SharedSailorProfileView {

    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, EditSailorProfile> {
    }

    interface Style extends CssResource {
        String edgeToEdge();
    }

    @UiField
    Style style;
    @UiField
    SharedResources res;
    @UiField(provided = true)
    SuggestedMultiSelection<SimpleCompetitorWithIdDTO> competitorSelectionUi;
    @UiField
    DivElement title;

    public EditSailorProfile(SharedSailorProfileView.Presenter presenter, FlagImageResolver flagImageResolver) {
        competitorSelectionUi = new CompetitorDisplayImpl(presenter.getFavoriteCompetitorsDataProvider(),
                flagImageResolver).selectionUi;
        initWidget(uiBinder.createAndBindUi(this));
        // TODO hide notificationsTextUi if the user's mail address is already verified
    }

    public void setEdgeToEdge(boolean edgeToEdge) {
        competitorSelectionUi.setStyleName(style.edgeToEdge(), edgeToEdge);
        competitorSelectionUi.getElement().getParentElement().removeClassName(res.mediaCss().column());
    }

    private class CompetitorDisplayImpl implements SuggestedMultiSelectionCompetitorDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;
        private final HasEnabled notifyAboutResultsUi;

        private CompetitorDisplayImpl(final SuggestedMultiSelectionCompetitorDataProvider dataProvider,
                FlagImageResolver flagImageResolver) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, StringMessages.INSTANCE.competitors(),
                    flagImageResolver);
            notifyAboutResultsUi = selectionUi.addNotificationToggle(new NotificationCallback() {
                @Override
                public void onNotificationToggled(boolean enabled) {
                    dataProvider.setNotifyAboutResults(enabled);
                }
            }, StringMessages.INSTANCE.notificationAboutNewResults());
            dataProvider.addDisplay(this);
        }

        @Override
        public void setSelectedItems(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }

        @Override
        public void setNotifyAboutResults(boolean notifyAboutResults) {
            notifyAboutResultsUi.setEnabled(notifyAboutResults);
        }
    }

    public void setEntry(SailorProfileEntry entry) {
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        title.setInnerText(entry.getName());
    }
}

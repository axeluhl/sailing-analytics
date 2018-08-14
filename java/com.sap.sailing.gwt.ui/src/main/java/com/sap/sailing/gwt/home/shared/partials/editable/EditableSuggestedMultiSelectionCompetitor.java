package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.function.Function;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorItemDescription;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EditableSuggestedMultiSelectionCompetitor
        extends EditableSuggestedMultiSelection<SimpleCompetitorWithIdDTO> {

    public EditableSuggestedMultiSelectionCompetitor(final SharedSailorProfileView.Presenter presenter,
            final FlagImageResolver flagImageResolver) {
        super(new Function<SimpleCompetitorWithIdDTO, IsWidget>() {

            @Override
            public IsWidget apply(SimpleCompetitorWithIdDTO t) {
                return new SuggestedMultiSelectionCompetitorItemDescription(t, flagImageResolver);
            }

        }, new CompetitorDisplayImpl(presenter.getCompetitorsDataProvider(), flagImageResolver).selectionUi);
        super.setText("Competitors");
    }

    private static class CompetitorDisplayImpl implements SuggestedMultiSelectionCompetitorDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;
        private final HasEnabled notifyAboutResultsUi;

        private CompetitorDisplayImpl(final SuggestedMultiSelectionCompetitorDataProvider dataProvider,
                FlagImageResolver flagImageResolver) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, StringMessages.INSTANCE.competitors(),
                    flagImageResolver);
            notifyAboutResultsUi = selectionUi.addNotificationToggle(dataProvider::setNotifyAboutResults,
                    StringMessages.INSTANCE.notificationAboutNewResults());
            dataProvider.addDisplay(this);
        }

        @Override
        public void setSelectedItems(Iterable<SimpleCompetitorWithIdDTO> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }

        @Override
        public void setNotifyAboutResults(boolean notifyAboutResults) {
            notifyAboutResultsUi.setEnabled(notifyAboutResults);
        }
    }

}

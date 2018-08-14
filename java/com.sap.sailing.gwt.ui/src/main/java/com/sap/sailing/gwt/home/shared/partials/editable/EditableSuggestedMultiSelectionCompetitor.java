package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.function.Function;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorItemDescription;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EditableSuggestedMultiSelectionCompetitor
        extends EditableSuggestedMultiSelection<SimpleCompetitorWithIdDTO> {

    public EditableSuggestedMultiSelectionCompetitor(final EditSailorProfileView.Presenter presenter,
            final FlagImageResolver flagImageResolver) {
        super(new Function<SimpleCompetitorWithIdDTO, IsWidget>() {

            @Override
            public IsWidget apply(SimpleCompetitorWithIdDTO t) {
                return new SuggestedMultiSelectionCompetitorItemDescription(t, flagImageResolver);
            }

        }, new CompetitorDisplayImpl(presenter.getDataProvider(), flagImageResolver).selectionUi,
                presenter.getDataProvider());
        super.setText("Competitors");
    }

    private static class CompetitorDisplayImpl implements SuggestedMultiSelectionCompetitorDataProvider.Display {
        private final SuggestedMultiSelection<SimpleCompetitorWithIdDTO> selectionUi;

        private CompetitorDisplayImpl(
                final SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display> dataProvider,
                FlagImageResolver flagImageResolver) {
            selectionUi = SuggestedMultiSelection.forCompetitors(dataProvider, StringMessages.INSTANCE.competitors(),
                    flagImageResolver);
            dataProvider.addDisplay(this);
        }

        @Override
        public void setSelectedItems(Iterable<SimpleCompetitorWithIdDTO> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }

        @Override
        public void setNotifyAboutResults(boolean notifyAboutResults) {
        }
    }

}

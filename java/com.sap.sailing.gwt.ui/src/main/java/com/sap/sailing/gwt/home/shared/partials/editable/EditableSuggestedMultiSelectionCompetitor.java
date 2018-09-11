package com.sap.sailing.gwt.home.shared.partials.editable;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.HeadlessSuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorItemDescription;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.StatefulSailorProfileDataProvider;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EditableSuggestedMultiSelectionCompetitor
        extends EditableSuggestedMultiSelection<SimpleCompetitorWithIdDTO> {

    public EditableSuggestedMultiSelectionCompetitor(final StatefulSailorProfileDataProvider dataProvider,
            final FlagImageResolver flagImageResolver) {
        this(dataProvider, flagImageResolver, false);
    }

    public EditableSuggestedMultiSelectionCompetitor(final StatefulSailorProfileDataProvider dataProvider,
            final FlagImageResolver flagImageResolver, final boolean headless) {
        super(competitor -> new SuggestedMultiSelectionCompetitorItemDescription(competitor, flagImageResolver),
                new CompetitorDisplayImpl(dataProvider, flagImageResolver).selectionUi, dataProvider,
                headless);
        super.setText(i18n.competitors());
    }

    private static class CompetitorDisplayImpl
            implements SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO> {
        private final SuggestedMultiSelectionView<SimpleCompetitorWithIdDTO> selectionUi;

        private CompetitorDisplayImpl(
                final SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO>> dataProvider,
                FlagImageResolver flagImageResolver) {
                selectionUi = HeadlessSuggestedMultiSelection.forCompetitors(dataProvider,
                        StringMessages.INSTANCE.competitors(), flagImageResolver);
            dataProvider.addDisplay(this);
        }

        @Override
        public void setSelectedItems(Iterable<SimpleCompetitorWithIdDTO> selectedItems) {
            selectionUi.setSelectedItems(selectedItems);
        }
    }

}

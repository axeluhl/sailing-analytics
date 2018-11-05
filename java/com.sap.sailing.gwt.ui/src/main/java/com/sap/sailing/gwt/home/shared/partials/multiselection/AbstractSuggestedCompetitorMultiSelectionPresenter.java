package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CompetitorSuggestionResult;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorSuggestionAction;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public abstract class AbstractSuggestedCompetitorMultiSelectionPresenter<D extends SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO>>
        extends AbstractSuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, D>
        implements SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, D> {

    private final ClientFactoryWithDispatch clientFactory;

    protected AbstractSuggestedCompetitorMultiSelectionPresenter(final ClientFactoryWithDispatch clientFactory) {
        super(SimpleCompetitorWithIdDTO::getIdAsString);
        this.clientFactory = clientFactory;
    }

    @Override
    public final String createSuggestionKeyString(SimpleCompetitorWithIdDTO value) {
        return value.getShortInfo();
    }

    @Override
    public final String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
        return value.getName();
    }

    @Override
    protected final void getSuggestions(Iterable<String> queryTokens, int limit,
            final SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
        final GetCompetitorSuggestionAction action = new GetCompetitorSuggestionAction(queryTokens, limit);
        clientFactory.getDispatch().execute(action, new AsyncCallback<CompetitorSuggestionResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify("Error while loading competitor suggestion", NotificationType.ERROR);
            }

            @Override
            public void onSuccess(CompetitorSuggestionResult result) {
                callback.setSuggestionItems(result.getValues());
            }
        });
    }

}

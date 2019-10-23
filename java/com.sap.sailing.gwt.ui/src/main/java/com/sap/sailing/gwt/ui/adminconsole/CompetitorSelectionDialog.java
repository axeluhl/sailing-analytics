package com.sap.sailing.gwt.ui.adminconsole;

import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * A dialog that can be used to select a single competitor. The dialog can be customized with a title and message. If a
 * competitor is specified as the object to select initially and that object is contained in the result of fetching the
 * competitors, it is selected in the table.
 * <p>
 * 
 * For fetching the competitors, a {@link Consumer} of an {@link AsyncCallback} must be provided. It is expected that
 * this consumer "consumes" the callback by obtaining a list of competitors, possibly asynchronously, and sending the
 * result to the callback's {@link AsyncCallback#onSuccess(Object)} method.<p>
 * 
 * The dialog's result is the {@link CompetitorDTO} selected when the dialog is confirmed using the OK button. When
 * no selection currently exists, {@code null} will result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CompetitorSelectionDialog extends DataEntryDialog<CompetitorDTO> {
    private final CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;

    public CompetitorSelectionDialog(SailingServiceAsync sailingService, UserService userService,
            ErrorReporter errorReporter, String title, String message,
            Consumer<AsyncCallback<Iterable<? extends CompetitorDTO>>> competitorProvider, StringMessages stringMessages,
            CompetitorDTO initialSelection,
            DialogCallback<CompetitorDTO> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(), /* validator */ null, callback);
        competitorTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages, errorReporter,
                /* multiSelection */ false, /* enablePager */ true, /* filter with boat */ false,
                /* filter without boat */ false);
        competitorProvider.accept(new AsyncCallback<Iterable<? extends CompetitorDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorLoadingCompetitors(caught.getMessage()), /* silentMode */ true);
            }

            @Override
            public void onSuccess(Iterable<? extends CompetitorDTO> result) {
                competitorTable.refreshCompetitorList(result);
                if (initialSelection != null) {
                    competitorTable.getSelectionModel().setSelected(initialSelection, /* selected */ true);
                }
            }
        });
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel result = new VerticalPanel();
        result.add(competitorTable);
        return result;
    }

    @Override
    protected CompetitorDTO getResult() {
        return competitorTable.getSelectionModel().getSelectedObject();
    }
}

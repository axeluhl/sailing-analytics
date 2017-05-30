package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * The extended {@link LeaderboardDescriptorWithEliminations}'s
 * {@link LeaderboardDescriptorWithEliminations#getEliminatedCompetitors() eliminated competitors} collection
 * is ignored by the constructor. It can safely be left empty. It will be filled by an asynchronous server
 * call triggered by the constructor. The resulting set will be injected into the
 * {@link LeaderboardDescriptorWithEliminations} object passed initially and will be returned
 * by {@link #getResult()} accordingly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaLeaderboardWithEliminationsEditDialog extends RegattaLeaderboardWithEliminationsDialog {
    public RegattaLeaderboardWithEliminationsEditDialog(SailingServiceAsync sailingService,
            Collection<StrippedLeaderboardDTO> otherExistingLeaderboards, Collection<RegattaDTO> existingRegattas,
            LeaderboardDescriptorWithEliminations leaderboardDescriptor, StringMessages stringMessages, ErrorReporter errorReporter,
            DialogCallback<LeaderboardDescriptorWithEliminations> callback) {
        super(sailingService, stringMessages.editRegattaLeaderboard(), leaderboardDescriptor,
                existingRegattas, otherExistingLeaderboards, stringMessages,
                errorReporter,
                new RegattaLeaderboardWithEliminationsDialog.LeaderboardParameterValidator(stringMessages,
                        otherExistingLeaderboards), callback);
        nameTextBox.setEnabled(false);
        nameTextBox.setText(leaderboardDescriptor.getName());
        for (int i=0; i<regattaLeaderboardsListBox.getItemCount(); i++) {
            if (regattaLeaderboardsListBox.getValue(i).equals(leaderboardDescriptor.getRegattaName())) {
                regattaLeaderboardsListBox.setSelectedIndex(i);
                break;
            }
        }
        regattaLeaderboardsListBox.setEnabled(false);
        displayNameTextBox.setText(leaderboardDescriptor.getDisplayName());
        createAndFillCompetitorEliminationPanel();
    }

    @Override
    protected Consumer<AsyncCallback<Collection<CompetitorDTO>>> getEliminatedCompetitorsRetriever() {
        eliminatedCompetitors = null;
        return callback->{
            if (eliminatedCompetitors != null) {
                callback.onSuccess(eliminatedCompetitors);
            } else {
                sailingService.getEliminatedCompetitors(nameTextBox.getValue(), new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(Collection<CompetitorDTO> result) {
                        eliminatedCompetitors = new HashSet<>(result);
                        callback.onSuccess(eliminatedCompetitors);
                    }
                });
            }
        };
    }
}

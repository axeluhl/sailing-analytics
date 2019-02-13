package com.sap.sse.security.ui.client.component.usergroup.users;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Suggest oracle for use in {@link UserGroupDetailPanel} which oracles the visible usernames.
 */
public class UserGroupSuggestOracle extends AbstractListSuggestOracle<UserDTO> {

    public UserGroupSuggestOracle(final UserManagementServiceAsync userManagementService,
            final StringMessages stringMessages) {

        userManagementService.getUserList(new AsyncCallback<Collection<UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadUsers());
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                setSelectableValues(result);
            }
        });
    }

    /**
     * @returns a {@link UserDTO}-object from the current selectable values of this oracle, which is associated with the
     *          given username.
     */
    public UserDTO fromString(final String userName) {
        if (this.getSelectableValues() == null) {
            throw new NullPointerException("Users are not loaded yet or could not be loaded.");
        }

        for (UserDTO user : this.getSelectableValues()) {
            if (user.getName().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    @Override
    protected String createSuggestionKeyString(UserDTO value) {
        return value.getName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(UserDTO value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(UserDTO value) {
        return this.getSelectableValues().stream().map(r -> createSuggestionKeyString(r)).collect(Collectors.toList());
    }
}

package com.sap.sse.security.ui.client.component.usergroup.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Suggest oracle for use in {@link UserGroupDetailPanel} which oracles the visible usernames.
 */
public class UserGroupSuggestOracle extends AbstractListSuggestOracle<UserDTO> {

    private final Collection<UserDTO> allUsers = new ArrayList<>();
    private final UserManagementServiceAsync userManagementService;
    private final StringMessages stringMessages;

    public UserGroupSuggestOracle(final UserManagementServiceAsync userManagementService,
            final StringMessages stringMessages) {
        this.userManagementService = userManagementService;
        this.stringMessages = stringMessages;
        refresh();
    }

    public void refresh() {
        userManagementService.getUserList(new AsyncCallback<Collection<UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadUsers());
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                allUsers.addAll(result);
                setSelectableValues(result);
            }
        });
    }

    /**
     * Clears the oracle suggestions, adds all existing users and finally removes the existing users from the oracle.
     */
    public void resetAndRemoveExistingUsers(Iterable<String> existingUsers) {
        Collection<UserDTO> users = new ArrayList<>(allUsers);
        users = users.stream().filter(u -> !Util.contains(existingUsers, u.getName())).collect(Collectors.toList());
        super.setSelectableValues(users);
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

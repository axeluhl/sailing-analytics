package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserListDataProvider extends AbstractDataProvider<UserDTO> {
    
    private UserManagementServiceAsync userManagementService;
    private TextBox filterBox;
    
    public UserListDataProvider(UserManagementServiceAsync userManagementService, TextBox filterBox) {
       this.userManagementService = userManagementService;
       this.filterBox = filterBox;
    }

    @Override
    protected void onRangeChanged(final HasData<UserDTO> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getFilteredSortedUserList(filterBox.getText(), new AsyncCallback<Collection<UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(caught.getMessage(), NotificationType.ERROR);
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                List<UserDTO> resultList = new ArrayList<>(result);
                List<UserDTO> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                final SelectionModel<? super UserDTO> selectionModel = display.getSelectionModel();
                Map<String, UserDTO> oldSelection = getSelectionByUsername(display, selectionModel);
                Set<UserDTO> toSelect = new HashSet<>();
                for (int i = start; i < end && i < resultList.size(); i++) {
                    final UserDTO e = resultList.get(i);
                    show.add(e);
                    if (oldSelection.containsKey(e.getName())) {
                        toSelect.add(e);
                    }
                }
                updateRowData(start, show);
                updateRowCount(result.size(), true);
                for (UserDTO userToSelect : toSelect) {
                    selectionModel.setSelected(userToSelect, true);
                }
            }
        });
    }

    private Map<String, UserDTO> getSelectionByUsername(HasData<UserDTO> display, SelectionModel<? super UserDTO> selectionModel) {
        Map<String, UserDTO> result = new HashMap<>();
        for (UserDTO user : display.getVisibleItems()) {
            if (selectionModel.isSelected(user)) {
                result.put(user.getName(), user);
            }
        }
        return result;
    }

    public void updateDisplays() {
        for (HasData<UserDTO> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
}

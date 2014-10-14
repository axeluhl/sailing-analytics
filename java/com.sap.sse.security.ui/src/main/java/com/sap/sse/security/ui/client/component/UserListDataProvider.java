package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserListDataProvider extends AbstractDataProvider<UserDTO> {
    
    private UserManagementServiceAsync userManagementService;
    private TextBox filterBox;
    
    public UserListDataProvider(UserManagementServiceAsync userManagementService, TextBox filterBox) {
       this.userManagementService = userManagementService;
       this.filterBox = filterBox;
    }

    @Override
    protected void onRangeChanged(HasData<UserDTO> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getFilteredSortedUserList(filterBox.getText(), new AsyncCallback<Collection<UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                List<UserDTO> resultList = new ArrayList<>(result);
                List<UserDTO> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; i < end && i < resultList.size(); i++){
                    show.add(resultList.get(i));
                }
                updateRowData(start, show);
                updateRowCount(result.size(), true);
            }
        });
    }

    public void updateDisplays(){
        for (HasData<UserDTO> hd : getDataDisplays()){
            onRangeChanged(hd);
        }
//        //TODO [D056866] SelectionChangeEvent.fire(singleSelectionModel);
    }
}

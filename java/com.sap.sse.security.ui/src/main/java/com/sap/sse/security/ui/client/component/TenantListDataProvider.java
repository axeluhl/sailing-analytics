package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class TenantListDataProvider extends AbstractDataProvider<Tenant> {
    private UserManagementServiceAsync userManagementService;
    private TextBox filterBox;
    
    public interface TenantListDataProviderChangeHandler {
        void onChange();
    }
    
    private final List<TenantListDataProviderChangeHandler> handlers;
    
    public TenantListDataProvider(UserManagementServiceAsync userManagementService, TextBox filterBox) {
       this.userManagementService = userManagementService;
       this.filterBox = filterBox;
       this.handlers = new ArrayList<>();
       
       filterBox.addChangeHandler(new ChangeHandler() {
           @Override
           public void onChange(ChangeEvent event) {
               TenantListDataProvider.this.updateDisplays();
           }
       });
       filterBox.addKeyUpHandler(new KeyUpHandler() {
           @Override
           public void onKeyUp(KeyUpEvent event) {
               TenantListDataProvider.this.updateDisplays();
           }
       });
    }
   
    @Override
    protected void onRangeChanged(final HasData<Tenant> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getTenants(new AsyncCallback<Collection<Tenant>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<Tenant> result) {
                List<Tenant> resultList = new ArrayList<>();
                for (Tenant tenant : result) {
                    if (tenant.getName().contains(filterBox.getText())) {
                        resultList.add(tenant);
                    }
                }
                List<Tenant> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; show.size() < end && i < resultList.size(); i++) {
                    final Tenant e = resultList.get(i);
                    show.add(e);
                }
                updateRowData(start, show);
                updateRowCount(resultList.size(), true);
                for (TenantListDataProviderChangeHandler handler : handlers) {
                    handler.onChange();
                }
            }
        });
    }
   
    public void updateDisplays() {
        for (HasData<Tenant> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
    
    public void addChangeHandler(TenantListDataProviderChangeHandler handler) {
        handlers.add(handler);
    }
}

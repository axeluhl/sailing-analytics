package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtServiceAsync;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;

/**
 * The only purpose of this class is to hold the value of the response of the
 * {@link EventDialog#testFileStorageService(FileStorageManagementGwtServiceAsync)} method It will notify all registered
 * observers on a change and directly after the registration, if the value is already true The value of this will only
 * change from false to true, so the value can also be seen as an alreadyChanged value If the test fails, the value will
 * not change due its lifetime
 * 
 * @author Robin Fleige (D067799)
 */
public class FileStorageServiceConnectionTestObservable {
    private List<FileStorageServiceConnectionTestObserver> observer = new ArrayList<>();
    private boolean value;

    public FileStorageServiceConnectionTestObservable(SailingServiceWriteAsync sailingServiceWrite) {
        value = false;
        sailingServiceWrite.testFileStorageServiceProperties(null, LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<FileStorageServicePropertyErrorsDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(FileStorageServicePropertyErrorsDTO result) {
                        if (result == null) {
                            testPassed();
                        }
                    }
                });
    }

    public void testPassed() {
        value = true;
        for (FileStorageServiceConnectionTestObserver observer : this.observer) {
            observer.onFileStorageServiceTestPassed();
        }
    }

    public boolean getValue() {
        return value;
    }

    public void registerObserver(FileStorageServiceConnectionTestObserver observer) {
        this.observer.add(observer);
        if (value) {
            observer.onFileStorageServiceTestPassed();
        }
    }

    public void unregisterObserver(FileStorageServiceConnectionTestObserver observer) {
        this.observer.remove(observer);
    }

    /**
     * The only use of this interface is to get notified if the response of the
     * {@link EventDialog#testFileStorageService(FileStorageManagementGwtServiceAsync)} method arrives Also see
     * {@link EventDialog.FileStorageServiceConnectionTestObservable}
     * 
     * @author Robin Fleige (D067799)
     */
    public static interface FileStorageServiceConnectionTestObserver {
        void onFileStorageServiceTestPassed();
    }
}

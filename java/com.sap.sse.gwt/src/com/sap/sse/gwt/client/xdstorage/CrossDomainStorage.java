package com.sap.sse.gwt.client.xdstorage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.Storage;

/**
 * Local and session {@link Storage} are isolated based on the document's <em>origin</em>. This cannot be influenced by
 * the {@code document.domain} property.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CrossDomainStorage {
    void setItem(String key, String value, AsyncCallback<Void> callback);

    void getItem(String key, AsyncCallback<String> callback);

    void removeItem(String key, AsyncCallback<Void> callback);

    void clear(AsyncCallback<Void> callback);

    void key(int index, AsyncCallback<String> callback);

    void getLength(AsyncCallback<Integer> callback);

    void getAllKeys(AsyncCallback<String[]> callback);
}

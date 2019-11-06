package com.sap.sse.gwt.client.xdstorage;

import java.util.function.Consumer;

import com.sap.sse.gwt.client.Storage;

/**
 * Local and session {@link Storage} are isolated based on the document's <em>origin</em>. This cannot be influenced by
 * the {@code document.domain} property.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CrossDomainStorage {
    void setItem(String key, String value, Consumer<Void> callback);

    void getItem(String key, Consumer<String> callback);

    void removeItem(String key, Consumer<Void> callback);

    void clear(Consumer<Void> callback);

    void key(int index, Consumer<String> callback);

    void getLength(Consumer<Integer> callback);

    void getAllKeys(Consumer<String[]> callback);
}

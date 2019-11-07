package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;

public class CrossDomainStorageFallingBackToLocalAfterTimeout implements CrossDomainStorage {
    /**
     * The path under which the HTML document running the {@link StorageMessagingEntryPoint} can be loaded.
     */
    private static final String STORAGE_MESSAGING_ENTRY_POINT_PATH = "gwt-base/StorageMessaging.html";

    /**
     * The timeout to wait for upon the first request to this cross-domain storage until the iframe containing the
     * storage in the target domain announces its readiness. If this timeout expires, the {@link #fallbackLocalStorage} is
     * used instead.
     */
    private static final Duration TIMEOUT_FOR_IFRAME_TO_RESPOND = Duration.ONE_SECOND.times(5);
    
    /**
     * Starts out as {@code null}. The constructor makes an effort to create an {@code iframe} element in the body of
     * the {@link Document} passed to the {@link #CrossDomainStorageFallingBackToLocalAfterTimeout(Document, String)
     * constructor}. Once the {@code iframe} has been established and called back to its parent window, the
     * {@link MessagePort} used for communicating with that {@code iframe} is passed to a consumer callback which sets
     * this field to a {@link CrossDomainStorageImpl} object using that {@link MessagePort}.
     * <p>
     * 
     * When a request comes in through any of the {@link CrossDomainStorage} methods and this field is still
     * {@code null}, a timer is started with the {@link #TIMEOUT_FOR_IFRAME_TO_RESPOND} duration, and the request is
     * recorded in the {@link #queuedWhileWaitingForIframe} queue. When the timer times out, or the {@link MessagePort}
     * is established---whichever comes first---the queued requests will be executed with the now decided
     * {@link CrossDomainStorage} which is then also set to this field. In case the timer timed out, this will be
     * a {@link LocalStorage} which does not use any {@link MessagePort} but maps all requests to the local storage
     * under the local origin.
     */
    private CrossDomainStorage storageToUse;
    
    /**
     * When request methods are called through any of the {@link CrossDomainStorage} interface methods and the
     * {@link #portToStorageMessagingEntryPoint} is not ready yet (still {@code null}), requests are queued in this list
     * until the moment when the message port is set after the iframe has announced its readiness, or the timer has timed
     * out, whichever comes first. Then, the requests are worked down, one after the other, using the {@link #crossDomainStorage}
     * through the iframe if it became ready in time, or using the {@link #fallbackLocalStorage} otherwise.
     */
    private final List<Consumer<CrossDomainStorage>> queuedWhileWaitingForIframe;
    
    /**
     * The timeout timer. Set if the first request is received through any of the {@link CrossDomainStorage} methods and the
     * {@link #storageToUse} is not yet set. Cancelled (if not {@code null} and reset to {@code null} when the {@link MessagePort}
     * connecting to the {@code iframe} has connected successfully and the {@link #storageToUse} is set to the "real"
     * {@link CrossDomainStorageImpl}.
     */
    private Timer timer;
    
    public CrossDomainStorageFallingBackToLocalAfterTimeout(Document documentInWhichToInsertMessagingIframe,
            String baseUrlForStorageMessagingEntryPoint) {
        this.queuedWhileWaitingForIframe = new LinkedList<>();
        MessagePort.createInDocument(documentInWhichToInsertMessagingIframe,
                baseUrlForStorageMessagingEntryPoint+(baseUrlForStorageMessagingEntryPoint.endsWith("/")?"":"/")+STORAGE_MESSAGING_ENTRY_POINT_PATH,
                result->{
                    GWT.log("connection to cross-domain storage at "+baseUrlForStorageMessagingEntryPoint+" established");
                    if (timer != null) {
                        GWT.log("cancelling cross-domain storage timout listener that would have resorted to local storage");
                        timer.cancel();
                        timer = null;
                    }
                    setStorageToUseAndWorkDownQueue(new CrossDomainStorageImpl(result, baseUrlForStorageMessagingEntryPoint));
                });
    }
    
    private void setStorageToUseAndWorkDownQueue(CrossDomainStorage storageToUse) {
        this.storageToUse = storageToUse;
        if (!queuedWhileWaitingForIframe.isEmpty()) {
            GWT.log("now executing "+queuedWhileWaitingForIframe.size()+" queued requests for cross-domain storage");
            for (final Consumer<CrossDomainStorage> queuedRequest : queuedWhileWaitingForIframe) {
                queuedRequest.accept(storageToUse);
            }
            queuedWhileWaitingForIframe.clear();
        }
    }

    private void runOrEnqueue(Consumer<CrossDomainStorage> request) {
        if (storageToUse != null) {
            request.accept(storageToUse);
        } else {
            queuedWhileWaitingForIframe.add(request);
            ensureTimerIsRunning();
        }
    }
    
    private void ensureTimerIsRunning() {
        if (timer == null) {
            GWT.log("cross-domain storage iframe not ready yet; waiting "+TIMEOUT_FOR_IFRAME_TO_RESPOND+" for message port");
            timer = new Timer() {
                @Override
                public void run() {
                    timer = null;
                    if (storageToUse == null) {
                        GWT.log("cross-domain storage timeout expired, using local storage "
                                + Window.Location.getProtocol() + "//" + Window.Location.getHost()
                                + " instead of cross-domain storage");
                        setStorageToUseAndWorkDownQueue(new LocalStorage());
                    }
                }
            };
            timer.schedule((int) TIMEOUT_FOR_IFRAME_TO_RESPOND.asMillis());
        }
    }

    @Override
    public void setItem(String key, String value, Consumer<Void> callback) {
        runOrEnqueue(storage->storage.setItem(key, value, callback));
    }

    @Override
    public void getItem(String key, Consumer<String> callback) {
        runOrEnqueue(storage->storage.getItem(key, callback));
    }

    @Override
    public void removeItem(String key, Consumer<Void> callback) {
        runOrEnqueue(storage->storage.removeItem(key, callback));
    }

    @Override
    public void clear(Consumer<Void> callback) {
        runOrEnqueue(storage->storage.clear(callback));
    }

    @Override
    public void key(int index, Consumer<String> callback) {
        runOrEnqueue(storage->storage.key(index, callback));
    }

    @Override
    public void getLength(Consumer<Integer> callback) {
        runOrEnqueue(storage->storage.getLength(callback));
    }
}

package com.sap.sse.gwt.client.xdstorage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.client.messaging.MessagePort;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent.Handler;
import com.sap.sse.gwt.client.xdstorage.impl.CrossDomainStorageImpl;

/**
 * Exposes the {@link CrossDomainStorage} interface immediately after object creation, but may start out without an
 * actual implementation objects to which all requests are delegated. Until this delegation target becomes available,
 * requests are enqueued. They are executed in order once the delegation target is
 * {@link #setStorageToUse(CrossDomainStorage) set}.
 * <p>
 * 
 * Optionally, a timeout can be set, defining how long to wait for the delegation target to be set after the first
 * {@link CrossDomainStorage} method call has been received, together with a {@link Supplier} for a
 * {@link CrossDomainStorage} delegation target implementation to default to if the timeout expires. If no such
 * timeout is set, requests could potentially remain queued forever.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DelegatingCrossDomainStorageFuture implements CrossDomainStorage {
    /**
     * Starts out as {@code null}. It can be set using {@link #setStorageToUse(CrossDomainStorage)} which will then also
     * process any requests queued.
     * <p>
     * 
     * When a request comes in through any of the {@link CrossDomainStorage} methods and this field is still
     * {@code null} and a timeout has been defined, a timer is started with the {@link #timeoutInMillis}
     * duration measured in milliseconds, and the request is recorded in the {@link #queuedWhileWaitingForStorage} queue.
     * When the timer times out, or the storage to use is provided---whichever comes first---the queued requests
     * will be executed with the now decided {@link CrossDomainStorage} which is then also set to this field. In case
     * the timer timed out, a default {@link Supplier} will be asked to supply its value which is then stored to
     * this field and used from that point forward.
     */
    private CrossDomainStorage storageToUse;
    
    /**
     * When request methods are called through any of the {@link CrossDomainStorage} interface methods and the
     * {@link #portToStorageMessagingEntryPoint} is not ready yet (still {@code null}), requests are queued in this list
     * until the moment when the message port is set after the iframe has announced its readiness, or the timer has timed
     * out, whichever comes first. Then, the requests are worked down, one after the other, using the {@link #crossDomainStorage}
     * through the iframe if it became ready in time, or using the {@link #fallbackLocalStorage} otherwise.
     */
    private final List<Consumer<CrossDomainStorage>> queuedWhileWaitingForStorage;
    
    /**
     * The timeout timer. Set if the first request is received through any of the {@link CrossDomainStorage} methods and the
     * {@link #storageToUse} is not yet set. Cancelled (if not {@code null} and reset to {@code null} when the {@link MessagePort}
     * connecting to the {@code iframe} has connected successfully and the {@link #storageToUse} is set to the "real"
     * {@link CrossDomainStorageImpl}.
     */
    private Timer timer;
    
    /**
     * If -1, no timeout applies and requests will remain in the queue until a non-{@code null} delegation target is set using
     */
    private final int timeoutInMillis;

    private final Supplier<CrossDomainStorage> defaultSupplier;
    
    /**
     * Creates an instance with no timeout.
     */
    public DelegatingCrossDomainStorageFuture() {
        this(/* timeout in millis */ -1, /* default storage provider */ null);
    }
    
    /**
     * Creates an instance with a specified timeout and a supplier of a {@link CrossDomainStorage} in case the timeout
     * expires.
     * 
     * @param timeoutInMillis
     *            if -1, no timeout logic applies
     * @param defaultSupplier
     *            must be non-{@code null} if {@code timeoutInMillis} is not -1
     */
    public DelegatingCrossDomainStorageFuture(final int timeoutInMillis, Supplier<CrossDomainStorage> defaultSupplier) {
        this.queuedWhileWaitingForStorage = new LinkedList<>();
        if (defaultSupplier == null && timeoutInMillis != -1) {
            throw new IllegalArgumentException("If a timeout of "+timeoutInMillis+"ms is provided, a valid default supplier must be provided, too.");
        }
        this.timeoutInMillis = timeoutInMillis;
        this.defaultSupplier = defaultSupplier;
    }
    
    public void setStorageToUse(CrossDomainStorage storageToUse) {
        if (storageToUse == null) {
            throw new NullPointerException("Must set a valid, non-null storage");
        }
        final CrossDomainStorage oldStorageToUse = this.storageToUse;
        this.storageToUse = storageToUse;
        if (oldStorageToUse != null) {
            GWT.log("Switching storage to use from type "+oldStorageToUse.getClass().getName()+
                    " to "+storageToUse.getClass().getName());
        }
        if (!queuedWhileWaitingForStorage.isEmpty()) {
            GWT.log("now executing "+queuedWhileWaitingForStorage.size()+" queued requests for cross-domain storage of type "+
                        storageToUse.getClass().getName());
            for (final Consumer<CrossDomainStorage> queuedRequest : queuedWhileWaitingForStorage) {
                queuedRequest.accept(storageToUse);
            }
            queuedWhileWaitingForStorage.clear();
        }
    }

    private void runOrEnqueue(Consumer<CrossDomainStorage> request) {
        if (storageToUse != null) {
            request.accept(storageToUse);
        } else {
            queuedWhileWaitingForStorage.add(request);
            if (timeoutInMillis != -1) {
                ensureTimerIsRunning();
            }
        }
    }
    
    private void ensureTimerIsRunning() {
        if (timer == null) {
            GWT.log("cross-domain storage iframe not ready yet; waiting "+timeoutInMillis+"ms for message port");
            timer = new Timer() {
                @Override
                public void run() {
                    timer = null;
                    if (storageToUse == null) {
                        GWT.log("cross-domain storage timeout expired, using local storage "
                                + Window.Location.getProtocol() + "//" + Window.Location.getHost()
                                + " instead of cross-domain storage");
                        setStorageToUse(defaultSupplier.get());
                    }
                }
            };
            timer.schedule(timeoutInMillis);
        }
    }

    @Override
    public HandlerRegistration addStorageEventHandler(Handler handler) {
        final HandlerRegistration[] result = new HandlerRegistration[1];
        runOrEnqueue(storage->result[0]=storage.addStorageEventHandler(handler));
        // not entirely safe if the addStorageEventHandler call above is really enqueued and the
        // HandlerRegistration.removeHandler call occurs before successful connection; the handler removal
        // then would fail with result[0] not yet being initialized
        return () -> runOrEnqueue(storage->{
                if (result[0] != null) {
                    result[0].removeHandler();
                }
            });
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

package com.sap.sse.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.realm.AuthorizingRealm;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.impl.Activator;

public abstract class AbstractUserStoreBasedRealm extends AuthorizingRealm {
    private static final Logger logger = Logger.getLogger(AbstractUserStoreBasedRealm.class.getName());
    private final Future<UserStore> userStore;

    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a default constructor makes it difficult
     * to get access to the user store implementation which may live in a bundle that this bundle has no direct access
     * to. Therefore, test cases must set the UserStore implementation by invoking {@link #setTestUserStore} before the
     * default constructor is invoked.
     */
    private static UserStore testUserStore;

    public static void setTestUserStore(UserStore theTestUserStore) {
        testUserStore = theTestUserStore;
    }

    public AbstractUserStoreBasedRealm() {
        super();
        BundleContext context = Activator.getContext();
        if (context != null) {
            userStore = createUserStoreFuture(context);
        } else {
            userStore = null;
        }
    }

    private Future<UserStore> createUserStoreFuture(BundleContext bundleContext) {
        final ServiceTracker<UserStore, UserStore> tracker = new ServiceTracker<>(bundleContext, UserStore.class, /* customizer */ null);
        tracker.open();
        final FutureTask<UserStore> result = new FutureTask<>(new Callable<UserStore>() {
            @Override
            public UserStore call() throws InterruptedException {
                try {
                    logger.info("Waiting for UserStore service...");
                    UserStore userStore = tracker.waitForService(0);
                    logger.info("Obtained UserStore service "+userStore);
                    return userStore;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                    throw e;
                }
            }
        });
        new Thread("ServiceTracker waiting for UserStore service") {
            @Override
            public void run() {
                try {
                    result.run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while waiting for UserStore service", e);
                }
            }
        }.start();
        return result;
    }

    protected UserStore getUserStore() {
        UserStore result;
        if (testUserStore != null) {
            result = testUserStore;
        } else {
            try {
                result = userStore.get();
            } catch (InterruptedException | ExecutionException e) {
                result = null;
                logger.log(Level.SEVERE, "Error retrieving user store", e);
            }
        }
        return result;
    }
}

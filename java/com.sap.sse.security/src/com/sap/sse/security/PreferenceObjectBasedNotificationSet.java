package com.sap.sse.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.Stoppable;
import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * <p>
 * Preferences available in the {@link UserStore} are held by the user as key. The preferences are stored as complex
 * objects that are being serialized to String to be saved in the DB. For convenience, the {@link UserStore} allows to
 * register {@link PreferenceConverter}s to also hold a deserialized version of the preferences in memory to speed up
 * the access without the need of a deserialization on demand.
 * </p>
 * 
 * <p>
 * When coming from the notification perspective, we have a domain object (e.g. competitor) and would like to know the
 * users to notify about this domain object. To calculate this Set of users we would need to loop over the specific
 * notification preferences for all users.
 * </p>
 * 
 * <p>
 * To speed up this part, the {@link PreferenceObjectBasedNotificationSet} holds an optimized association of who to notify about
 * specific objects. On preference changes for the given key in the given {@link UserStore}, the associations are being
 * updated to make the model always reflect the current state of the associations. Due to this the calculation of the
 * users to notify is a simple lookup.
 * </p>
 * 
 * <p>
 * To use this, implement a subclass in which the {@link #calculateObjectsToNotify(Object)} method determines the domain
 * objects from a preference object for which the user needs to be notified.
 * </p>
 *
 * @param <PrefT>
 *            The type of preference object on which the calculation of the objects to notify is based on.
 * @param <T>
 *            The type of object for which the users to notify is managed.
 */
public abstract class PreferenceObjectBasedNotificationSet<PrefT, T> implements Stoppable {
    private static final Logger logger = Logger.getLogger(PreferenceObjectBasedNotificationSet.class.getName());

    private UserStore store;
    private final PreferenceObjectListener<PrefT> listener = new PreferenceObjectListenerImpl();

    /**
     * The keys are the objects to notify for. The values are the names of the users to notify about an object.
     */
    private final Map<T, Set<String>> notifications = new HashMap<>();

    private final BundleContext context;

    private final String key;

    private final ServiceTracker<UserStore, UserStore> tracker;
    
    private final NamedReentrantReadWriteLock lock;

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public PreferenceObjectBasedNotificationSet(String key, BundleContext context) {
        this.key = key;
        this.context = context;
        if (context == null) {
            this.tracker = null;
        } else {
            this.tracker = new ServiceTracker<UserStore, UserStore>(context, UserStore.class, new Cutomizer());
            this.tracker.open();
        }
        this.lock = new NamedReentrantReadWriteLock(getClass().getName()+" for "+key, /* fair */ false);
    }

    /**
     * Constructor used to directly work with a given {@link UserStore}.
     */
    public PreferenceObjectBasedNotificationSet(String key, UserStore store) {
        this(key, /* context */ (BundleContext) null);
        this.store = store;
        store.addPreferenceObjectListener(key, listener, true);
    }

    @Override
    public void stop() {
        if (tracker != null) {
            tracker.close();
        }
        removeStore();
    }

    private void removeStore() {
        if (store != null) {
            store.removePreferenceObjectListener(listener);
            store = null;
        }
        LockUtil.lockForWrite(lock);
        try {
            notifications.clear();
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    /**
     * Determines the domain objects for which the user needs to be notified.
     * 
     * @param preference
     *            a notification preference of some sort which typically describes or contains a set of domain objects
     *            the user is "interested" in and for changes of which the user wants to be notified.
     * @return the domain objects described by the {@code preference} object thta the user is "interested" in
     */
    protected abstract Collection<T> calculateObjectsToNotify(PrefT preference);

    public Iterable<String> getUsersnamesToNotifyFor(T object) {
        LockUtil.lockForRead(lock);
        try {
            return new HashSet<>(Util.get(notifications, object, Collections.emptySet()));
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    /**
     * The given consumer will be called for every user that needs to be notified about the given object.
     * Users without a verified email address will be skipped.
     */
    public void forUsersWithVerifiedEmailMappedTo(T object, Consumer<User> consumer) {
        for (String username : getUsersnamesToNotifyFor(object)) {
            // User objects can change silently. So we just keep the usernames and get the associated user objects on
            // the fly.
            User user = store.getUserByName(username);
            if (user == null) {
                logger.log(Level.SEVERE, "Could not get User for name \"" + username + "\"");
            } else {
                // we can not exclude users without verified email address when calculating the association because this
                // state could have changed meanwhile. So this need to be done on the fly.
                if (user.isEmailValidated()) {
                    consumer.accept(user);
                }
            }
        }
    }

    private class PreferenceObjectListenerImpl implements PreferenceObjectListener<PrefT> {
        @Override
        public void preferenceObjectChanged(String username, String key, PrefT oldPreference, PrefT newPreference) {
            Collection<T> oldObjectsToNotify = oldPreference == null ? Collections.emptySet()
                    : calculateObjectsToNotify(oldPreference);
            Collection<T> newObjectsToNotify = newPreference == null ? Collections.emptySet()
                    : calculateObjectsToNotify(newPreference);

            Set<T> objectsToRemove = new HashSet<>(oldObjectsToNotify);
            objectsToRemove.removeAll(newObjectsToNotify);

            Set<T> objectsToAdd = new HashSet<>(newObjectsToNotify);
            objectsToAdd.removeAll(oldObjectsToNotify);

            LockUtil.lockForWrite(lock);
            try {
                for (T objectToRemove : objectsToRemove) {
                    Util.removeFromValueSet(notifications, objectToRemove, username);
                }
                for (T objectToAdd : objectsToAdd) {
                    Util.addToValueSet(notifications, objectToAdd, username);
                }
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
    }

    private class Cutomizer implements ServiceTrackerCustomizer<UserStore, UserStore> {
        @Override
        public UserStore addingService(ServiceReference<UserStore> reference) {
            UserStore store = context.getService(reference);
            if (PreferenceObjectBasedNotificationSet.this.store != null
                    && PreferenceObjectBasedNotificationSet.this.store != store) {
                logger.severe("Multiple " + UserStore.class.getSimpleName()
                        + " instances found. Only one instance is handled.");
            } else {
                PreferenceObjectBasedNotificationSet.this.store = store;
                store.addPreferenceObjectListener(key, listener, true);
            }
            return store;
        }

        @Override
        public void modifiedService(ServiceReference<UserStore> reference, UserStore service) {
        }

        @Override
        public void removedService(ServiceReference<UserStore> reference, UserStore service) {
            if (PreferenceObjectBasedNotificationSet.this.store == service) {
                removeStore();
            }
        }
    }
}

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
 * To speed up this part, the PreferenceObjectBasedNotificationSet holds an optimized association of who to notify about
 * specific objects. On preference changes for the given key in the given {@link UserStore}, the associations are being
 * updated to make the model always reflect the current state of the associations. Due to this the calculation of the
 * users to notify is a simple lookup.
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

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public PreferenceObjectBasedNotificationSet(String key, BundleContext context) {
        this.key = key;
        this.context = context;
        tracker = new ServiceTracker<UserStore, UserStore>(context, UserStore.class, new Cutomizer());
    }

    /**
     * Constructor used to work with a given {@link UserStore}.
     */
    public PreferenceObjectBasedNotificationSet(String key, UserStore store) {
        this.key = key;
        this.store = store;
        context = null;
        tracker = null;
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
        // TODO use write lock
        synchronized (notifications) {
            notifications.clear();
        }
    }

    protected abstract Collection<T> calculateObjectsToNotify(PrefT preference);

    public Iterable<String> getUsersnamesToNotifyFor(T object) {
        // TODO use read lock
        synchronized (notifications) {
            return new HashSet<>(Util.get(notifications, object, Collections.emptySet()));
        }
    }

    /**
     * The given consumer will be called for every user that needs to be notified about the given object.
     */
    public void forUsersMappedTo(T object, Consumer<User> consumer) {
        for (String username : getUsersnamesToNotifyFor(object)) {
            // User objects can change silently. So we just keep the usernames and get the associated user objects on
            // the fly.
            User user = store.getUserByName(username);
            if (user == null) {
                logger.log(Level.SEVERE, "Could not get User for name \"" + username + "\"");
            } else {
                consumer.accept(user);
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

            // TODO use write lock
            synchronized (notifications) {
                for (T objectToRemove : objectsToRemove) {
                    Util.removeFromValueSet(notifications, objectToRemove, username);
                }
                for (T objectToAdd : objectsToAdd) {
                    Util.addToValueSet(notifications, objectToAdd, username);
                }
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
            // Should we do anything here? the preference key could have changed, but does this make any sense?
        }

        @Override
        public void removedService(ServiceReference<UserStore> reference, UserStore service) {
            if (PreferenceObjectBasedNotificationSet.this.store == service) {
                removeStore();
            }
        }
    }
}

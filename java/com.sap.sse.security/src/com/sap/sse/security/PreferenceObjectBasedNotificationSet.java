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

import com.sap.sse.common.Util;

/**
 * Holds an optimized association of who to notify about specific objects.
 *
 * @param <PrefT>
 *            The type of preference object on which the calculation of the objects to notify is based on.
 * @param <T>
 *            The type of object for which the users to notify is managed.
 */
public abstract class PreferenceObjectBasedNotificationSet<PrefT, T> {
    private static final Logger logger = Logger.getLogger(PreferenceObjectBasedNotificationSet.class.getName());

    private final UserStore store;
    private final PreferenceObjectListener<PrefT> listener = new PreferenceObjectListenerImpl();

    /**
     * The keys are the objects to notify for. The values are the names of the users to notify about an object.
     */
    private final Map<T, Set<String>> notifications = new HashMap<>();

    public PreferenceObjectBasedNotificationSet(String key, UserStore store) {
        this.store = store;
        store.addPreferenceObjectListener(key, listener, true);
    }

    public void stop() {
        store.removePreferenceObjectListener(listener);
    }

    protected abstract Collection<T> calculateObjectsToNotify(PrefT preference);

    private Iterable<String> getUsersToNotifyFor(T object) {
        // TODO use read lock
        synchronized (notifications) {
            return new HashSet<>(Util.get(notifications, object, Collections.emptySet()));
        }
    }

    /**
     * The given consumer will be called for every user that needs to be notified about the given object.
     */
    public void forUsersMappedTo(T object, Consumer<User> consumer) {
        for (String username : getUsersToNotifyFor(object)) {
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
}

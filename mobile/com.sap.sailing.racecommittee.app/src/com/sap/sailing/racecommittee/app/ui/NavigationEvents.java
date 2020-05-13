package com.sap.sailing.racecommittee.app.ui;

import android.support.v4.app.Fragment;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public enum NavigationEvents {
    INSTANCE;

    public void detach(Fragment startTimeFragment) {
        for (NavigationListener navigationListener:fragmentAttachListeners){
            navigationListener.onFragmentDetach(startTimeFragment);
        }
    }

    public void attach(Fragment startTimeFragment) {
        for (NavigationListener navigationListener:fragmentAttachListeners){
            navigationListener.onFragmentAttach(startTimeFragment);
        }
    }

    public interface NavigationListener {
        void onFragmentAttach(Fragment fragment);

        void onFragmentDetach(Fragment fragment);
    }

    private final List<NavigationListener> fragmentAttachListeners = new CopyOnWriteArrayList<>();

    public void subscribeFragmentAttachment(NavigationListener listener) {
        fragmentAttachListeners.add(listener);
    }

    public void unSubscribeFragmentAttachment(NavigationListener listener) {
        fragmentAttachListeners.remove(listener);
    }
}

package com.sap.sailing.android.shared.application;

import java.lang.ref.WeakReference;

import android.content.Context;

/**
 * We use this wrapper object to ensure that nobody will use the static context freely and nobody holds strong
 * references to it.
 */
public class StringContext {

    private final WeakReference<Context> context;

    public StringContext(WeakReference<Context> context) {
        this.context = context;
    }

    /**
     * Return a localized string from the application's package's default string table.
     *
     * @param resId Resource id for the string
     * @return the string
     */
    public final String getString(int resId) {
        Context ctx = context.get();
        if (ctx == null) {
            throw new IllegalStateException("Lost reference to context");
        }
        return ctx.getString(resId);
    }

}

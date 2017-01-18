package com.sap.sse.gwt.client.shared.perspective;

import java.util.LinkedList;
import java.util.Queue;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default settings of a component/perspective and its subcomponents if there are any.
 * It supplies components with initial settings and determines whether the settings of components are storable or not.
 * This abstract implementation provides settings storage support if the storable settings either implement {@link SettingsMap}
 * or {@link GenericSerializableSettings}.
 * There are two kinds of settings which are stored separately - <b>global settings</b> and <b>context specific settings</b>.
 * <ul>
 * <li>Global settings are the settings which are applied globally to all components</li>
 * <li>Context specific settings have higher precedence than global settings and are applied to components only if the current
 * context (e.g. event or race) matches the context when the settings have been stored.</li>
 * </ul>
 * That means that context specific settings are stored per context (e.g. race or event) whereas global settings are stored
 * globally for all possible contexts (independent of event or race).
 * 
 * 
 * @author Vladislav Chumak
 *
 * @param <L> The {@link ComponentLifecycle} type of the root component/perspective containing all the settings for itself and its subcomponents
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 */
public abstract class AbstractComponentContextWithSettingsStorage<L extends ComponentLifecycle<S, ?>, S extends Settings> extends AbstractComponentContext<L, S> {
    
    /**
     * Manages the persistence layer of settings.
     */
    private final SettingsStorageManager<S> settingsStorageManager;
    
    /**
     * Contains {@link SettingsReceiverCallback}s which are waiting to receive the initial settings of the root component.
     */
    private Queue<SettingsReceiverCallback<S>> settingsReceiverCallbacks = new LinkedList<>();
    
    /**
     * Current initial/default settings for the whole settings tree which corresponds to the root component and its subcomponents.
     */
    private S currentDefaultSettings = null;
    
    /**
     * 
     * @param rootLifecycle The {@link ComponentLifecycle} of the root component/perspective
     * @param settingsStorageManager The {@link SettingsStorageManager} to be used access stored settings and store new settings
     */
    public AbstractComponentContextWithSettingsStorage(L rootLifecycle, SettingsStorageManager<S> settingsStorageManager) {
        super(rootLifecycle);
        this.settingsStorageManager = settingsStorageManager;
    }
    
    /**
     * Initialises the instance with initial settings. This method may be called only once during the whole lifecycle
     * of this instance. The call of this method is mandatory, otherwise it will not be possible to obtain initial settings.
     * 
     * @see #initInitialSettings(OnSettingsLoadedCallback)
     */
    public void initInitialSettings() {
        initInitialSettings(null);
    }
    
    /**
     * Initialises the instance with initial settings. This method may be called only once during the whole lifecycle
     * of this instance. The call of this method is mandatory, otherwise it will not be possible to obtain initial settings.
     * 
     * @param onInitialSettingsLoaded Callback to be called when the settings initialisation finishes
     * @see #initInitialSettings()
     */
    public void initInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        if(currentDefaultSettings != null) {
            throw new IllegalStateException("Settings have been already initialized. You may only call this method once.");
        }
        S systemDefaultSettings = rootLifecycle.createDefaultSettings();
        settingsStorageManager.retrieveDefaultSettings(systemDefaultSettings, new OnSettingsLoadedCallback<S>() {

            @Override
            public void onError(Throwable caught, S fallbackDefaultSettings) {
                if(onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onError(caught, fallbackDefaultSettings);
                }
            }

            @Override
            public void onSuccess(S result) {
                currentDefaultSettings = result;
                if(onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onSuccess(rootLifecycle.cloneSettings(result));
                }
                SettingsReceiverCallback<S> callback;
                while((callback = settingsReceiverCallbacks.poll()) != null) {
                    callback.receiveSettings(rootLifecycle.cloneSettings(result));
                }
            }
        });
    }
    
    /**
     * Retrieve settings for the root component managed by this context.
     * The provided callback gets called when initial/default settings are available. That means
     * if the initialisation of this instance is finished, the provided callback is called
     * immediately. If initialisation is not done yet, then the callback gets called when
     * the initialisation gets finished and thus, initial settings are available.
     * Make sure to call {@link #initInitialSettings()} when using this method, otherwise
     * the provided callback will be never called.
     * 
     * @param settingsReceiverCallback The callback which supplies the caller with initial settings
     */
    public void receiveInitialSettings(SettingsReceiverCallback<S> settingsReceiverCallback) {
        if(currentDefaultSettings == null) {
            settingsReceiverCallbacks.add(settingsReceiverCallback);
        } else {
            settingsReceiverCallback.receiveSettings(currentDefaultSettings);
        }
    }

    /**
     * Stores the {@link Settings} of the passed {@link Component} in the default component settings tree.
     * Make sure to call this method only when {@link #hasMakeCustomDefaultSettingsSupport(Component)}
     * method returns {@code true} for the passed {@link Component}.
     * 
     * @param component The component which the passed {@link Settings} correspond to
     * @param newDefaultSettings The {@link Settings} to be stored
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        // Perspective<? extends Settings> parentPerspective =
        // component.getComponentTreeNodeInfo().getParentPerspective();
        //
        // final S newRootSettings;
        // if(parentPerspective == null) {
        // if(component instanceof Perspective) {
        // //root perspective is updating its perspective own settings
        // Perspective<? extends Settings> rootPerspective = (Perspective<? extends Settings>) component;
        // newRootSettings = (S) newDefaultSettings;
        // } else {
        // //root is a single component
        // newRootSettings = (S) newDefaultSettings;
        // }
        // } else {
        // // propagate updated settings to parent and get root settings
        // newRootSettings = (S) propagateSettingsToRootPerspective(parentPerspective, component, newDefaultSettings);
        // }
        // this.currentDefaultSettings = rootLifecycle.cloneSettings(newRootSettings);
        // storeNewDefaultSettings(newRootSettings);
    }
    
    private void storeNewDefaultSettings(S newRootSettings) {
        S globalSettings = extractGlobalSettings(newRootSettings);
        if(globalSettings != null) {
            settingsStorageManager.storeGlobalSettings(globalSettings);
        }
        S contextSpecificSettings = extractContextSpecificSettings(newRootSettings);
        if(contextSpecificSettings != null) {
            settingsStorageManager.storeContextSpecificSettings(contextSpecificSettings);
        }
    }

    /**
     * Extracts context specific settings from provided {@link Settings} of the root component.
     * 
     * @param newRootSettings The settings of the root component
     * @return The context specific settings extracted, or {@code null} if there aren't any
     * context specific settings to be stored
     * @see AbstractComponentContextWithSettingsStorage
     */
    protected abstract S extractContextSpecificSettings(S newRootSettings);

    /**
     * Extracts global settings from provided {@link Settings} of the root component.
     * 
     * @param newRootSettings The settings of the root component
     * @return The global settings extracted, or {@code null} if there aren't any
     * context specific settings to be stored
     */
    protected abstract S extractGlobalSettings(
            S newRootSettings);

    // private<T extends Settings> PerspectiveCompositeSettings<?> propagateSettingsToRootPerspective(Perspective<T>
    // perspective, Component<? extends Settings> replaceComponent, Settings replaceComponentNewDefaultSettings) {
    // Map<String, Settings> originalSettingsPerComponent =
    // perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsPerComponentId();
    // Map<String, Settings> newSettingsPerComponent = new HashMap<>();
    // String replaceComponentId = replaceComponent.getId();
    // for (Entry<String, Settings> entry : originalSettingsPerComponent.entrySet()) {
    // String componentId = entry.getKey();
    // if(replaceComponentId.equals(componentId)) {
    // newSettingsPerComponent.put(replaceComponent.getId(), replaceComponentNewDefaultSettings);
    // } else {
    // newSettingsPerComponent.put(componentId, entry.getValue());
    // }
    // }
    //
    // PerspectiveCompositeSettings<T> allSettings = new
    // PerspectiveCompositeSettings<>(perspectiveLifecycleWithAllSettings.getPerspectiveSettings(),
    // newSettingsPerComponent);
    //
    // Perspective<? extends Settings> parentPerspective =
    // perspective.getComponentTreeNodeInfo().getParentPerspective();
    // if(parentPerspective != null) {
    // return propagateSettingsToRootPerspective(parentPerspective, perspective, allSettings);
    // } else {
    // return allSettings;
    // }
    // }
    
    /**
     * Gets the last error occurred during settings initialisation.
     * 
     * @return The last error as {@link Throwable}, if an error occurred, otherwise {@code null}
     */
    public Throwable getLastError() {
        return settingsStorageManager.getLastError();
    }
    
    /**
     * Gets the current default {@link Settings} of the root component managed
     * by this {@link ComponentContext}. The returned {@link Settings} should
     * contain all settings for the root component and its subcomponents.
     * 
     * @return The {@link Settings} of the root component
     * @throws IllegalStateException When the instance has not been initialised yet
     * @see #initInitialSettings()
     */
    @Override
    public S getDefaultSettings() {
        if(currentDefaultSettings == null) {
            throw new IllegalStateException("Settings have not been initialized yet.");
        }
        return rootLifecycle.cloneSettings(currentDefaultSettings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        if(!component.hasSettings()) {
            return false;
        }
        Settings settings = component.getSettings();
        if(settings instanceof SettingsMap || settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }
    
}

package com.sap.sse.common.settings;

import com.sap.sse.common.settings.generic.Setting;

/**
 * Common superclass for custom Settings used by Components. The following example shows how to implement custom
 * Settings:
 * 
 * <pre>
 * <code>
 * public final class ExampleSettings extends AbstractSettings {
 *     private transient StringSetting myString;
 *     private transient BooleanSetting myBoolean;
 *     
 *     // Used to create instances for a component
 *     public ExampleSettings() {
 *     }
 *     
 *     // Used to create nested settings
 *     public ExampleSettings(String name, AbstractSettings parentSettings) {
 *         super(name, parentSettings);
 *     }
 *     
 *     @Override
 *     protected void addChildSettings() {
 *         myString = new UUIDSetting("myString", this)
 *         myBoolean = new BooleanSetting("myBoolean", this);
 *     }
 *     
 *     public String getMyString() {
 *         return this.myString.getValue();
 *     }
 *     
 *     public void setMyString(String myString) {
 *         return this.myString.setValue(myString);
 *     }
 *     
 *     public boolean isMyBoolean() {
 *         return this.myBoolean.getValue();
 *     }
 *     
 *     public void setMyBoolean(boolean myBoolean) {
 *         return this.myBoolean.setValue(myBoolean);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * To correctly support several kinds of Serialization (Java, GWT and Custom), you need to ensure the following:
 * <ul>
 *   <li>the Settings class must have a default constructor</li>
 *   <li>child settings fields need to be transient</li>
 *   <li>child settings instances must be initialized in {@link #addChildSettings()} method</li>
 * </ul>
 * 
 * If a custom Settings class only has {@link Setting} oder {@link Settings} children that are correctly attached by
 * calling their constructor with a name and the parent {@link Settings} you do not need to implement
 * {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()}. There are generic implementations of those methods that are
 * based on the child {@link Setting}s which should be sufficient for most cases.
 *
 */
public abstract class AbstractSettings  implements Settings {
}

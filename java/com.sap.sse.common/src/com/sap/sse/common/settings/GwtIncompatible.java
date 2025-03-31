package com.sap.sse.common.settings;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Private version of com.google.gwt.core.shared.GwtIncompatible.
 * GWT doesn't check for the concrete annotation class but only checks if there is an annotation named GwtIncompatible.
 * With this private annotation we can flag something as GWT incompatible without introducing a dependency to GWT core.
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.TYPE, ElementType.METHOD,
    ElementType.CONSTRUCTOR, ElementType.FIELD })
@Documented
public @interface GwtIncompatible {
  /**
   * An attribute that can be used to explain why the code is incompatible.
   * A GwtIncompatible annotation can have any number of attributes; attributes
   * are for documentation purposes and are ignored by the GWT compiler.
   */
  String value() default "";
}
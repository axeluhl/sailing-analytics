package com.sap.sailing.datamining.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a package, class or interface to show, that:</br>
 * <ul>
 *      <li>A package contains classes, that declare methods, that are marked as data mining function</li>
 *      <li>A class/interface contains methods, that are marked as data mining function</li>
 * </ul>
 * 
 * @see {@link Dimension}, {@link SideEffectFreeValue}
 * @author Lennart Hensler (D054527)
 *
 */
@Documented
@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DefinesDataMiningFunctions {

}

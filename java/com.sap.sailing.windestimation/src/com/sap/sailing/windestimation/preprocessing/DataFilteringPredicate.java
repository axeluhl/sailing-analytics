package com.sap.sailing.windestimation.preprocessing;

import java.util.function.Predicate;

/**
 * Predicate for data pre-processing applied in context of machine learning internals of wind estimation
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 *            The type of instance being tested
 */
public interface DataFilteringPredicate<T> extends Predicate<T> {

}

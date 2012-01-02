package com.sap.sailing.domain.base;

/**
 * Some values, particularly those obtained from real-world measurements, are not always accurate. Some
 * values are derived by interpolating or extrapolating data series obtained through measurement or
 * even estimation. Some values are simply guessed by humans and entered into the system.<p>
 * 
 * All those values have a certain level of confidence. In case multiple sources of information about the
 * same entity or phenomenon are available, knowing the confidence of each value helps in weighing and
 * averaging these values more properly than would be possible without a confidence value.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface HasConfidence {

}

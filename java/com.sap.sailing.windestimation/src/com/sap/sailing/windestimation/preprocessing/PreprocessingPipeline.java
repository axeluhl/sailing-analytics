package com.sap.sailing.windestimation.preprocessing;

/**
 * Transforms input of type <From> to output of type <To> by applying custom pre-processing logic.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <From>
 *            Input type
 * @param <To>
 *            Output type
 */
public interface PreprocessingPipeline<From, To> {

    /**
     * Pre-processes input and converts it to output type.
     * 
     * @param input
     *            Input to pre-process
     * @return Pre-processed output
     */
    To preprocessInput(From input);

}

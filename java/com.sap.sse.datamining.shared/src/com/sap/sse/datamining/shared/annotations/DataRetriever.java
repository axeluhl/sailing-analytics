package com.sap.sse.datamining.shared.annotations;

/**
 * Annotation to mark the concrete data retrievers for a specific domain.<br />
 * This is necessary to link the base data types with the data retrievers, to be able
 * to construct the queries more easily.<br />
 * <br />
 * Attributes:
 * <ul>
 *   <li><b>dataType</b> The type, that is retrieved.</ li>
 *   <li><b>groupName</b> The group name for corresponding retrievers. This is used
 *                        for hierarchical base data types.</ li>
 *   <li><b>level</b> The level of this retriever in the group, where 0 is the highest
 *                    level. This means, that the level 0 retriever retrieves the data,
 *                    that is less specific (e.g. HasTrackedRaceContext is less specific
 *                    than HasTrackedLegContext).</ li>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 */
public @interface DataRetriever {
    
    public static final String DEFAULT_GROUP = "Default";
    
    /**
     * The type, that is retrieved.
     */
    public Class<?> dataType();
    
    /**
     * The group name for corresponding retrievers. This is used for hierarchical base data types.<br />
     * The default group is specified in {@link #DEFAULT_GROUP}.
     */
    public String groupName() default DEFAULT_GROUP;
    
    /**
     * The level of this retriever in the group, where 0 is the highest level. This means, that the level 0 retriever
     * retrieves the data, that is less specific (e.g. HasTrackedRaceContext is less specific than HasTrackedLegContext).<br />
     * <br />
     * The default level is 0.
     */
    public int level() default 0;

}

package com.sap.sse.datamining.components.management;

public interface MemoryInfoProvider {

    /**
     * Returns the amount of free memory in the Java Virtual Machine.
     *
     * @return  an approximation to the total amount of memory currently
     *          available for future allocated objects, measured in bytes.
     */
    long freeMemory();

    /**
     * Returns the total amount of memory in the Java virtual machine.
     * The value returned by this method may vary over time, depending on
     * the host environment.
     *
     * @return  the total amount of memory currently available for current
     *          and future objects, measured in bytes.
     */
    long totalMemory();

}

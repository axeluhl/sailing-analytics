package com.sap.sailing.monitoring.sysinfo;

/**
 * Provides detailed information about process resources
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 10, 2013
 */
public interface ProcessInformation {

    public abstract String getName();

    public abstract long getPid();

    /**
     * @return Maximum number of open files for this
     * process. This can be different from system wide setting!
     */
    public abstract long getMaxOpenFiles();

    /**
     * @return Number of running threads
     */
    public abstract long getCurrentThreadCount();

    /**
     * @return Number of currently open files
     */
    public abstract long getCurrentOpenFileCount();

    /**
     * @return The amount of virtualized memory segments available
     * to this process. This number can be slightly higher than
     * the physical memory because it can also be mapped to a hard disk or such.
     */
    public abstract long getVirtualMemorySize();

    /**
     * @return Shared memory describes the amount of memory that
     * is available to this process but also to others.
     */
    public abstract long getSharedMemorySize();

    /**
     * @return Resident memory size describes the amount of RAM that
     * is used to hold real process data. It's max value is bound to physically
     * available memory.
     */
    public abstract long getResidentMemorySize();

    /**
     * @return The CPU time spend in kernel where 1000=100%
     */
    public abstract long getKernelCPUTime();

    /**
     * @return The CPU time spend in userspace where 1000=100%
     */
    public abstract long getUserCPUTime();

}
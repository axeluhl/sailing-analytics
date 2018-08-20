package com.sap.sailing.monitoring.sysinfo;

import java.util.Map;

import org.hyperic.sigar.NetConnection;

/**
 * Provides detailed system information from different sources. Please be aware
 * of the fact that some methods are optimized to work in a linux environment.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 8, 2013
 */
public interface SystemInformation {
    
    /**
     * @return Returns the number of local open ports. This holds for IPv4 and for IPv6. This
     *          number can also contain connections made to external servers where we are the client.
     * @throws Exception
     */
    public long getOutbundOpenConnectionCount() throws Exception;
    
    /**
     * @return The number of currently open TCP and UDP sockets system wide that are inbound 
     * (this computer is the server and has been connected by a client).
     * @throws Exception
     */
    public long getInboundOpenConnectionCount() throws Exception;

    /**
     * @return Number of maximum allowed open sockets system wide
     * @throws Exception
     */
    public long getMaxOpenSockets() throws Exception;
    
    /**
     * @return Number of currently open files system wide
     * @throws Exception 
     */
    public long getOpenFiles() throws Exception;
    
    /**
     * @return Number of maximum open files allowed system wide
     * @throws Exception
     */
    public long getMaxOpenFiles() throws Exception;
    
    /**
     * @return Number of maximum of in-memory inodes allowed system wide
     * @throws Exception
     */
    public long getMaxInMemoryINodes() throws Exception;
    
    /**
     * @param pid
     * @return Useful information (like limits or currently open files) about a process.
     */
    public ProcessInformation getProcessInformation(long pid);
    
    /**
     * @return The PID of the process this code is running in
     * @throws Exception
     */
    public long getPid() throws Exception;
    
    /**
     * @return The last load average computed
     * @throws Exception
     */
    public double getLastLoadAverage() throws Exception;
    
    /**
     * @return Open network Connections (providing information about port and address)
     * @throws Exception
     */
    public NetConnection[] getOpenNetworkConnections(int flags) throws Exception;
    
    /**
     * @return The number of currently running processes
     * @throws Exception 
     */
    public long getTotalRunningProcesses() throws Exception;
    
    /**
     * @return The number of currently blocked processes (waiting for I/O)
     * @throws Exception 
     */
    public long getTotalStoppedProcesses() throws Exception;

    /**
     * @return The number of currently zombies
     * @throws Exception 
     */
    public long getTotalZombieProcesses() throws Exception;

    /**
     * @return The number of all registered processes
     * @throws Exception 
     */
    public long getTotalProcesses() throws Exception;

    /**
     * @return The number of memory bytes that are available
     * @throws Exception
     */
    public long getFreeMemoryGlobal() throws Exception;
    
    /**
     * @return The filesystem and the number of free inodes for it
     * @throws Exception
     */
    public Map<String, Long> getFreeFileNodes() throws Exception;
    
    /**
     * @return Number of used sockets system wide
     */
    public long getUsedSockets();

    /**
     * @return Number of system wide allocated tcp connections
     */
    public long getAllocatedTCPConnections();
    
    /**
     * @return Actually used TCP connections
     */
    public long getTCPConnectionsInUse();

    /**
     * @return Free memory in bytes of JVM
     */
    public long getFreeMemoryJVM();
    
    /**
     * @return Memory in bytes allocatable for every object
     */
    public long getTotalMemoryJVM();
}

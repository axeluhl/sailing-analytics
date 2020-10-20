package com.sap.sse.landscape.application;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface ApplicationProcess<ShardingKey, MetricsT extends ApplicationProcessMetrics,
MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>>
        extends Process<RotatingFileBasedLog, MetricsT> {
    /**
     * @return the replica set to which this process belongs
     */
    ApplicationReplicaSet<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getReplicaSet();
    
    /**
     * Tries to shut down an OSGi application server process cleanly by sending the "shutdown" OSGi command to this
     * process's OSGi console using the {@link #getTelnetPortToOSGiConsole() telnet port}. If the instance hasn't
     * terminated after {@code timeout} after having received this shutdown request, if {@code forceAfterTimeout} is
     * {@code true}, a hard kill command will be used terminate the virtual machine and {@code false} is returned;
     * otherwise ({@code forceAfterTimeout==false}), {@code false} will be returned after the timeout period.
     * 
     * @return {@code true} if the clean shutdown has succeeded, {@code false} otherwise. Note that therefore the result
     *         does not indicate whether the process was finally gone; with {@code forceAfterTimeout==true} callers can
     *         assume that no matter what the result of this call, the VM will finally be gone, but with this logic it's
     *         possible even with a hard shutdown to figure out that a hard shutdown was actually required and the clean
     *         shutdown didn't work.
     */
    boolean tryCleanShutdown(Duration timeout, boolean forceAfterTimeout);
}

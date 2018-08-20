package com.sap.sailing.monitoring.sysinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * @see SystemInformation
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 8, 2013
 */
public class SystemInformationImpl implements SystemInformation {

    private Sigar sigar_manager;
    private OperatingSystemMXBean java_manager;

    private static SystemInformation singleton = null;

    private long used_sockets = -1;
    private long allocated_tcp_connections = -1;
    private long used_tcp_connections = -1;

    /**
     * @return An instance in case SIGAR can be activated. You always have to check
     *          for a null value.
     */
    public synchronized static SystemInformation getInstance() {
        if (singleton == null) {
            try {
                singleton = new SystemInformationImpl();
            } catch (SigarException ex) {
                singleton = null;
            }
        }

        return singleton;
    }

    public SystemInformationImpl() throws SigarException {
        Sigar.load(); /* check if native libraries can be loaded */

        this.sigar_manager = new Sigar();
        this.java_manager = ManagementFactory.getOperatingSystemMXBean();
    }

    protected String[] readProc(String path) {
        String line = ""; StringBuffer buf = new StringBuffer();
        BufferedReader reader = null;
        try {
             reader = new BufferedReader(new FileReader(path));
             while ( (line = reader.readLine() ) != null) {
                 buf.append(line).append("\n");
             }

        } catch (Exception ex) {
            return new String[]{}; /* return empty result */
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    /* ignore */
                }
            }
        }
        return buf.toString().split("\\n");
    }

    protected void parseSockStat() {
        String[] sockstat = new String[]{};
        try {
             sockstat = readProc("/proc/" + getPid() + "/net/sockstat");
        } catch (Exception e) {
            /* ignore, something went wrong with PID extraction */
        }

        /* seems that there are no local socket counts,
         * try to use global ones */
        if (sockstat.length == 0) {
            sockstat = readProc("/proc/net/sockstat");
        }

        for (String line : sockstat) {
            String[] parts = line.split(" ");
            if (parts[0].equalsIgnoreCase("sockets:"))
                this.used_sockets = Long.parseLong(parts[2].trim());

            if (parts[0].equalsIgnoreCase("TCP:")) {
                this.allocated_tcp_connections = Long.parseLong(parts[8].trim());
                this.used_tcp_connections = Long.parseLong(parts[2].trim());
            }
        }
    }

    @Override
    public ProcessInformation getProcessInformation(long pid) {
        return new ProcessInformationImpl(pid, sigar_manager);
    }

    @Override
    public long getOutbundOpenConnectionCount() throws Exception {
        int flags = NetFlags.CONN_TCP | NetFlags.CONN_UDP | NetFlags.CONN_CLIENT;
        NetConnection[] connections = sigar_manager.getNetConnectionList(flags);
        return connections.length;
    }

    @Override
    public long getInboundOpenConnectionCount() throws Exception {
        int flags = NetFlags.CONN_TCP | NetFlags.CONN_UDP | NetFlags.CONN_SERVER;
        NetConnection[] connections = sigar_manager.getNetConnectionList(flags);
        return connections.length;
    }

    @Override
    public long getMaxOpenSockets() throws Exception {
        return getMaxOpenFiles();
    }

    @Override
    public long getUsedSockets() {
        parseSockStat();
        return this.used_sockets;
    }

    @Override
    public long getAllocatedTCPConnections() {
        parseSockStat();
        return this.allocated_tcp_connections;
    }

    @Override
    public long getTCPConnectionsInUse() {
        parseSockStat();
        return this.used_tcp_connections;
    }

    @Override
    public long getOpenFiles() {
        String[] val = readProc("/proc/sys/fs/file-nr");

        if(val.length > 0)
            return Long.parseLong(val[0].split("\t| ")[0].trim());
        return -1;
    }

    @Override
    public long getMaxOpenFiles() throws Exception {
        String[] val = readProc("/proc/sys/fs/file-max");

        if (val.length > 0)
            return Long.parseLong(val[0].trim());
        return -1;
    }

    @Override
    public long getMaxInMemoryINodes() throws Exception {
        String[] val = readProc("/proc/sys/fs/inode-max");

        if (val.length > 0)
            return Long.parseLong(val[0].trim());
        return -1;
    }

    @Override
    public long getPid() throws Exception {
        return sigar_manager.getPid();
    }

    @Override
    public double getLastLoadAverage() throws Exception {
        return java_manager.getSystemLoadAverage();
    }

    @Override
    public NetConnection[] getOpenNetworkConnections(int flags) throws Exception {
        return sigar_manager.getNetConnectionList(flags);
    }

    @Override
    public long getTotalRunningProcesses() throws Exception {
        return sigar_manager.getProcStat().getRunning();
    }

    @Override
    public long getTotalStoppedProcesses() throws Exception {
        return sigar_manager.getProcStat().getStopped();
    }

    @Override
    public long getTotalZombieProcesses() throws Exception {
        return sigar_manager.getProcStat().getZombie();
    }

    @Override
    public long getTotalProcesses() throws Exception {
        return sigar_manager.getProcStat().getTotal();
    }

    @Override
    public long getFreeMemoryGlobal() throws Exception {
        return sigar_manager.getMem().getFree();
    }

    @Override
    public long getFreeMemoryJVM() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getTotalMemoryJVM() {
        return Runtime.getRuntime().totalMemory();
    }

    @Override
    public Map<String, Long> getFreeFileNodes() throws Exception {
        Map<String, Long> result = new HashMap<String, Long>();
        for (FileSystem fs : sigar_manager.getFileSystemList()) {
            if (fs.getType() == FileSystem.TYPE_LOCAL_DISK)
                result.put(fs.getDirName(), sigar_manager.getFileSystemUsage(fs.getDirName()).getFreeFiles());
        }
        return result;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("");
        try {

            result.append("General:\n");
            result.append(getLastLoadAverage()).append(" (load avg)").append("\n");
            result.append(getTotalRunningProcesses()).append(" (total processes running)").append("\n");
            result.append(getTotalStoppedProcesses()).append(" (total processes stopped)").append("\n");
            result.append(getTotalZombieProcesses()).append(" (total processes zombie)").append("\n");
            result.append(getTotalProcesses()).append(" (total)").append("\n");

            result.append("\nMemory:\n");
            result.append(getFreeMemoryGlobal()/1000 + "kb (free, not cached, not inactive)\n");
            result.append(getFreeMemoryJVM()/1000 + "kb (free JVM)\n");
            result.append((getTotalMemoryJVM()-getFreeMemoryJVM())/1000 + "kb (used JVM)\n");
            result.append(getTotalMemoryJVM()/1000 + "kb (total JVM)\n");

            result.append("\nOpen files:\n")
                    .append(getOpenFiles()).append(" / ").append(getUsedSockets()).append(" (normal / sockets)").append("\n")
                    .append(getMaxOpenFiles()).append(" (max, also socket max)").append("\n")
                    .append(getMaxInMemoryINodes()).append(" (max in-memory inodes)").append("\n");

            result.append("\nNet:\n" + getTCPConnectionsInUse() + " (tcp conn in use)\n" + getAllocatedTCPConnections() + " (alloc'd tcp conn)\n" + getUsedSockets() + " (used sockets)").append("\n");

            result.append(getOutbundOpenConnectionCount()).append(" (outbound conn count)").append("\n")
                .append(getInboundOpenConnectionCount()).append(" (inbound conn count)").append("\n");

            int flags = NetFlags.CONN_TCP | NetFlags.CONN_UDP | NetFlags.CONN_SERVER;
            NetConnection[] connections = getOpenNetworkConnections(flags);

            result.append("\nCalling Process Statistics:\n");
            result.append(getProcessInformation(getPid()) + "\n");

            result.append("\nOpen Local Ports:\n");
            for (NetConnection ns : connections) {
                if (ns.getRemoteAddress().equalsIgnoreCase("0.0.0.0") ||
                        ns.getRemoteAddress().equalsIgnoreCase("127.0.0.1") ||
                        ns.getRemoteAddress().equalsIgnoreCase("::"))
                result.append(ns.getLocalAddress() + ":" + ns.getLocalPort()).append("\n");
            }

            flags = NetFlags.CONN_TCP | NetFlags.CONN_UDP | NetFlags.CONN_CLIENT;
            connections = getOpenNetworkConnections(flags);

            result.append("\nOutbound Connections:\n");
            for (NetConnection ns : connections) {
                result.append(ns.getLocalAddress() + ":" + ns.getLocalPort() + " -> " + ns.getRemoteAddress() + ":" + ns.getRemotePort()).append("\n");
            }

            result.append("\nInbound Connections:\n");
            for (NetConnection ns : connections) {
                /* ignore connections to self */
                if ( !(ns.getRemoteAddress().equalsIgnoreCase("0.0.0.0") ||
                        ns.getRemoteAddress().equalsIgnoreCase("127.0.0.1") ||
                        ns.getRemoteAddress().equalsIgnoreCase("::"))
                   )
                result.append(ns.toString()).append("\n");
            }

            result.append("\nContents of TMP directory (java.io.tmpdir)").append("\n");
            result.append(Arrays.toString(new File(System.getProperty("java.io.tmpdir")).listFiles()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result.toString();
    }

    public static void main(String[] args) {
        try {
            System.out.println(SystemInformationImpl.getInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

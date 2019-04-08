package com.sap.sailing.monitoring.sysinfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcFd;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;

/**
 * @see ProcessInformation
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 9, 2013
 */
public class ProcessInformationImpl implements ProcessInformation {
    
    private long pid = -1;
    private String name = "";
    
    private long max_open_files = -1;
    private long current_open_file_count = -1;
    
    private long current_thread_count = -1;
    
    private long virtual_memory_size = -1;
    private long resident_memory_size = -1;
    private long shared_memory_size = -1;
    
    private long kernel_cpu_time = -1;
    private long user_cpu_time = -1;
    
    public ProcessInformationImpl(long pid, Sigar sigar) {
        this.pid = pid;
        
        try {
            name = sigar.getProcExe(pid).getName();

            ProcCpu cpu = sigar.getProcCpu(pid);
            kernel_cpu_time = cpu.getSys();
            user_cpu_time = cpu.getUser();
            
            ProcMem mem = sigar.getProcMem(pid);
            virtual_memory_size = mem.getSize();
            resident_memory_size = mem.getResident();
            shared_memory_size = mem.getShare();
            
            ProcFd fd = sigar.getProcFd(pid);
            current_open_file_count = fd.getTotal();
            
            ProcState state = sigar.getProcState(pid);
            current_thread_count = state.getThreads();
            
            if (!System.getProperty("os.name").startsWith("Windows")) {
                parseLimits();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected String[] readProc(String path) {
        BufferedReader reader = null;
        StringBuffer buf = new StringBuffer("");
        try {
            reader = new BufferedReader(new FileReader(path));
            String line = "";
            while ( (line = reader.readLine() ) != null) {
                buf.append(line).append("\n");
            }
        } catch (Exception ex) {
            return new String[]{};
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    return new String[]{};
                }
            }
        }

        return buf.toString().split("\\n");
    }
    
    protected void parseLimits() {        
        String[] limits = new String[]{};
        limits = readProc("/proc/" + getPid() + "/limits");
        
        for (String line : limits) {
            String[] parts = line.split(" ");
            if (parts[1].equalsIgnoreCase("open") && parts[2].equalsIgnoreCase("files"))
                this.max_open_files = Long.parseLong(parts[14].trim());
        }
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getPid()
     */
    @Override
    public long getPid() {
        return this.pid;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getMaxOpenFiles()
     */
    @Override
    public long getMaxOpenFiles() {
        return this.max_open_files;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getCurrentThreadCount()
     */
    @Override
    public long getCurrentThreadCount() {
        return this.current_thread_count;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getCurrentOpenFileCount()
     */
    @Override
    public long getCurrentOpenFileCount() {
        return this.current_open_file_count;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getVirtualMemorySize()
     */
    @Override
    public long getVirtualMemorySize() {
        return this.virtual_memory_size;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getSharedMemorySize()
     */
    @Override
    public long getSharedMemorySize() {
        return this.shared_memory_size;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getResidentMemorySize()
     */
    @Override
    public long getResidentMemorySize() {
        return this.resident_memory_size;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getKernelCPUTime()
     */
    @Override
    public long getKernelCPUTime() {
        return this.kernel_cpu_time;
    }
    
    /* (non-Javadoc)
     * @see com.sap.sailing.monitoring.sysinfo.ProcessInformation#getUserCPUTime()
     */
    @Override
    public long getUserCPUTime() {
        return this.user_cpu_time;
    }
    
    public String toString() {
        return new StringBuffer()
            .append(getName() + " PID: " + getPid()).append("\n")
            .append("CPU: " + (getKernelCPUTime()*0.001) + " (Kernel) " + (getUserCPUTime()*0.001) + " (User)").append("\n")
            .append("Mem: " + getResidentMemorySize()/1000 + "kb (Resident) " + getVirtualMemorySize()/1000 + "kb (Virtual) " + getSharedMemorySize()/1000 + "kb (shared)").append("\n")
            .append("Thread Count: " + getCurrentThreadCount() + " Open Files: " + getCurrentOpenFileCount() + " Max Open Files: " + getMaxOpenFiles())
            .toString();
    }
}

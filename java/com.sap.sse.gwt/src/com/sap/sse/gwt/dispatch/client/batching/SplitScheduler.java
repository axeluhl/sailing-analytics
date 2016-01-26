package com.sap.sse.gwt.dispatch.client.batching;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Convenience to enqueue and execute ScheduledCommands using the GWT scheduler.
 */
public class SplitScheduler {
    
    private static final SplitScheduler INSTANCE = new SplitScheduler();
    
    private List<ScheduledCommand> pendingCommands = new ArrayList<ScheduledCommand>();
    
    private boolean scheduled = false;
    
    private SplitScheduler() {
    }

    /**
     * Repeating command instance that picks up the next element in the queue of pending commands and executes it.
     */
    private final RepeatingCommand repeatingCommand = new RepeatingCommand() {
        @Override
        public boolean execute() {
            ScheduledCommand commandToExecute = pendingCommands.remove(0);
            
            try {
                commandToExecute.execute();
            } catch (Exception e) {
                if(GWT.getUncaughtExceptionHandler() != null)  {
                    GWT.getUncaughtExceptionHandler().onUncaughtException(e);
                }
            }
            scheduled = !pendingCommands.isEmpty();
            return scheduled;
        }
    };
    
    public static SplitScheduler get() {
        return INSTANCE;
    }
    
    /**
     * Add a command to the queue and executes it using the gwt scheduler.
     * 
     * @param command
     */
    public void schedule(ScheduledCommand command) {
        pendingCommands.add(command);
        
        if(!scheduled) {
            Scheduler.get().scheduleIncremental(repeatingCommand);
            scheduled = true;
        }
    }

}

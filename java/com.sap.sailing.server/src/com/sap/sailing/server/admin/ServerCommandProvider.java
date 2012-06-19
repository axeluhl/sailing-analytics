package com.sap.sailing.server.admin;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class ServerCommandProvider implements CommandProvider {
    public void _hello(CommandInterpreter ci) {
        ci.print("Hello, " + ci.nextArgument());
    }

    public String getHelp() {
        return "\thello - say hello\n";
    }
}
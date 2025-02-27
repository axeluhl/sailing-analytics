package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class AIAgentConfigurationPanel extends SimplePanel {
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;

    public AIAgentConfigurationPanel(final Presenter presenter, final StringMessages stringMessages) {
        this.sailingServiceWrite = presenter.getSailingService();
        this.userService = presenter.getUserService();
        this.stringMessages = stringMessages;
        this.errorReporter = presenter.getErrorReporter();
    }
}

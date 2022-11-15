package com.sap.sailing.domain.common;

public enum MailInvitationType {
    // Legacy invitations do not need any Branch.io information because
    // links are constructed by different implementation.
    LEGACY(/* supportsOpenRegattas */ false,
           /* branchIOinviteURL */ null,
           /* branchIOcheckinPath */ null,
           /* branchIOopenRegattaURL */ null),
    SailInsight1(/* supportsOpenRegattas */ false,
                 /* branchIOinviteURL */ BranchIOConstants.SAILINSIGHT_APP_BRANCHIO,
                 /* branchIOcheckinPath */ BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH,
                 /* branchIOopenRegattaURL */ null),
    SailInsight2(/* supportsOpenRegattas */ true,
                 /* branchIOinviteURL */ BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO,
                 /* branchIOcheckinPath */ BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO_PATH,
                 /* branchIOopenRegattaURL */ BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO),
    SailInsight3(/* supportsOpenRegattas */ true,
                 /* branchIOinviteURL */ BranchIOConstants.SAILINSIGHT_3_APP_BRANCHIO,
                 /* branchIOcheckinPath */ BranchIOConstants.OPEN_REGATTA_3_APP_BRANCHIO_PATH,
                 /* branchIOopenRegattaURL */ BranchIOConstants.OPEN_REGATTA_3_APP_BRANCHIO);

    public static final String SYSTEM_PROPERTY_NAME = "com.sap.sailing.domain.tracking.MailInvitationType";
    
    private MailInvitationType(boolean supportsOpenRegattas, String branchIOinviteURL, String branchIOcheckinPath,
            String branchIOopenRegattaURL) {
        this.supportsOpenRegattas = supportsOpenRegattas;
        this.branchIOinviteURL = branchIOinviteURL;
        this.branchIOcheckinPath = branchIOcheckinPath;
        this.branchIOopenRegattaURL = branchIOopenRegattaURL;
    }
    
    public boolean isSupportsOpenRegattas() {
        return supportsOpenRegattas;
    }
    public String getBranchIOinviteURL() {
        return branchIOinviteURL;
    }
    public String getBranchIOcheckinPath() {
        return branchIOcheckinPath;
    }
    public String getBranchIOopenRegattaURL() {
        return branchIOopenRegattaURL;
    }

    private final boolean supportsOpenRegattas;
    private final String branchIOinviteURL;
    private final String branchIOcheckinPath;
    private final String branchIOopenRegattaURL;
}
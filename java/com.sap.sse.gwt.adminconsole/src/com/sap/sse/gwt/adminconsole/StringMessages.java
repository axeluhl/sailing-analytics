package com.sap.sse.gwt.adminconsole;

import com.google.gwt.i18n.client.Messages;

public interface StringMessages extends Messages {
    String upload();

    String remove();

    String removeResult(String status, String message);

    String uploadSuccessful();

    String fileUploadResult(String status, String message);

    String removeUploadedFile();
    
    String pleaseOnlyUploadContentYouHaveAllUsageRightsFor();
    
    String send();

    String version(String buildVersion);

    String unknown();

    String refresh();

    String explainReplicasRegistered();

    String explainConnectionsToMaster();

    String stopAllReplicas();

    String connectToMaster();

    String stopConnectionToMaster();

    String errorStartingReplication(String b, String c, String message);

    String loading();

    String registeredAt(String string);

    String dropReplicaConnection();

    String replicables();

    String averageNumberOfOperationsPerMessage();

    String numberOfQueueMessagesSent();

    String averageMessageSize();

    String totalSize();

    String totalNumberOfOperations();

    String explainNoConnectionsFromReplicas();

    String warningServerIsReplica();

    String replicatingFromMaster(String hostname, int messagingPort, int servletPort, String messagingHostname,
            String exchangeName, String string);

    String explainNoConnectionsToMaster();

    String errorFetchingReplicaData(String message);

    String connect();

    String enterMaster();

    String cancel();

    String hostname();

    String explainReplicationHostname();

    String exchangeHost();

    String explainExchangeHostName();

    String exchangeName();

    String explainReplicationExchangeName();

    String messagingPortNumber();

    String explainReplicationExchangePort();

    String servletPortNumber();

    String explainReplicationServletPort();

    String ok();
}

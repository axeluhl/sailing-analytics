package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SerializationDummyDTO implements IsSerializable {
    public ProcessDTO mongoProcessDTO;
    public AwsInstanceDTO awsInstanceDTO;
    public SailingApplicationReplicaSetDTO<String> sailingApplicationReplicaSetDTO;
}

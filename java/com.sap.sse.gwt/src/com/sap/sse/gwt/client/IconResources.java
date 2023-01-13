package com.sap.sse.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconResources extends ClientBundle {
    
    public static final IconResources INSTANCE = GWT.create(IconResources.class);

    @Source("images/change-acl.png")
    ImageResource changeACLIcon();

    @Source("images/change-ownership.png")
    ImageResource changeOwnershipIcon();

    @Source("images/migrate-change-ownership.png")
    ImageResource changeMigrateOwnershipIcon();

    @Source("images/remove.png")
    ImageResource removeIcon();
    
    @Source("images/edit.png")
    ImageResource editIcon();
    
    @Source("images/boat_registrations.png")
    ImageResource boatRegistrations();

    @Source("images/download.png")
    ImageResource downloadIcon();
    
    @Source("images/refresh.png")
    ImageResource refreshIcon();
    
    @Source("images/scale.png")
    ImageResource scaleIcon();

    @Source("images/scaleUp.png")
    ImageResource scaleUpIcon();

    @Source("images/scaleDown.png")
    ImageResource scaleDownIcon();

    @Source("images/archive.png")
    ImageResource archiveIcon();

    @Source("images/load-balancer.png")
    ImageResource loadBalancerIcon();

    @Source("images/launch-another-replica-set-on-this-master.png")
    ImageResource launchAnotherReplicaSetOnThisMasterIcon();

    @Source("images/unlink.png")
    ImageResource unlinkIcon();

    @Source("images/red_gears.png")
    ImageResource redGearsIcon();

    @Source("images/move.png")
    ImageResource moveIcon();
    
    @Source("images/shardmanagement.png")
    ImageResource shardManagementIcon();
}

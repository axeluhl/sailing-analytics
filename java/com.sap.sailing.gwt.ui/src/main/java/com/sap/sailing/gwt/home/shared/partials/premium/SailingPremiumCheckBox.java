package com.sap.sailing.gwt.home.shared.partials.premium;

import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.premium.PremiumCheckBox;

public class SailingPremiumCheckBox extends PremiumCheckBox{

    public SailingPremiumCheckBox(String label, UserService userService, WildcardPermission permission,
            OwnershipDTO ownership, AccessControlListDTO acl) {
        super(label, userService, permission, ownership, acl);
    }
    
    @Override
    protected Image createPremiumIcon() {
        return new Image(SailingPremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }

}

package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class DeviceConfigurationImpl extends NamedImpl implements DeviceConfiguration {

    private static final long serialVersionUID = 6084215932610324314L;
    
    private RegattaConfiguration regattaConfiguration;
    
    private List<String> allowedCourseAreaNames;
    private String resultsMailRecipient;
    private List<String> byNameDesignerCourseNames;
    private final UUID id;

    public DeviceConfigurationImpl(RegattaConfiguration regattaConfiguration, UUID id, String name) {
        super(name);
        this.regattaConfiguration = regattaConfiguration;
        this.id = id;
    }
    
    @Override
    public UUID getId() {
        return id;
    }

    public void setRegattaConfiguration(RegattaConfiguration proceduresConfiguration) {
        this.regattaConfiguration = proceduresConfiguration;
    }

    @Override
    public RegattaConfiguration getRegattaConfiguration() {
        return regattaConfiguration;
    }

    @Override
    public List<String> getAllowedCourseAreaNames() {
        return allowedCourseAreaNames;
    }

    public void setAllowedCourseAreaNames(List<String> newAllowedCourseAreaNames) {
        this.allowedCourseAreaNames = newAllowedCourseAreaNames;
    }

    @Override
    public String getResultsMailRecipient() {
        return resultsMailRecipient;
    }

    public void setResultsMailRecipient(String resultsMailRecipient) {
        this.resultsMailRecipient = resultsMailRecipient;
    }
    
    @Override
    public List<String> getByNameCourseDesignerCourseNames() {
        return byNameDesignerCourseNames;
    }

    public void setByNameDesignerCourseNames(List<String> byNameDesignerCourseNames) {
        this.byNameDesignerCourseNames = byNameDesignerCourseNames;
    }
    
    public DeviceConfiguration copy() {
        DeviceConfigurationImpl copyConfiguration = new DeviceConfigurationImpl(regattaConfiguration.clone(), getId(), getName());
        copyConfiguration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        copyConfiguration.setByNameDesignerCourseNames(byNameDesignerCourseNames);
        copyConfiguration.setResultsMailRecipient(resultsMailRecipient);
        return copyConfiguration;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION;
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getId());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID id) {
        return new TypeRelativeObjectIdentifier(id.toString());
    }
}

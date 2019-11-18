package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.NamedDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class CourseTemplateDTO extends NamedDTO implements SecuredDTO {

    private static final long serialVersionUID = 4919350862920588863L;

    private UUID uuid;

    // use concrete types instead of Iterable for GWT serialization
    private ArrayList<MarkTemplateDTO> markTemplates = new ArrayList<>();
    private ArrayList<WaypointTemplateDTO> waypointTemplates = new ArrayList<>();
    private HashMap<MarkTemplateDTO, UUID> associatedRoles = new HashMap<>();
    private ArrayList<String> tags = new ArrayList<>();

    private String optionalImageUrl;

    private RepeatablePartDTO repeatablePart;
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    private Integer defaultNumberOfLaps;
    
    public CourseTemplateDTO() {
        // for GWT serialization
    }

    public CourseTemplateDTO(UUID uuid, String name, Iterable<MarkTemplateDTO> markTemplates,
            Iterable<WaypointTemplateDTO> waypointTemplates, Map<MarkTemplateDTO, UUID> associatedRoles,
            String optionalImageUrl, Iterable<String> tags, RepeatablePartDTO repeatablePart,
            Integer defaultNumberOfLaps) {
        super(name);
        this.uuid = uuid;
        this.defaultNumberOfLaps = defaultNumberOfLaps;
        Util.addAll(markTemplates, this.markTemplates);
        Util.addAll(waypointTemplates, this.waypointTemplates);
        this.associatedRoles.putAll(associatedRoles);
        this.optionalImageUrl = optionalImageUrl;
        Util.addAll(tags, this.tags);
        this.repeatablePart = repeatablePart;
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public void setAccessControlList(AccessControlListDTO createAccessControlListDTO) {
        securityInformation.setAccessControlList(createAccessControlListDTO);
    }

    @Override
    public void setOwnership(OwnershipDTO createOwnershipDTO) {
        securityInformation.setOwnership(createOwnershipDTO);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    @Override
    public HasPermissions getPermissionType() {
        return SecuredDomainType.COURSE_TEMPLATE;
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return getTypeRelativeObjectIdentifier(getUuid().toString());
    }

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String dbId) {
        return new TypeRelativeObjectIdentifier(dbId);
    }

    public UUID getUuid() {
        return uuid;
    }

    public ArrayList<MarkTemplateDTO> getMarkTemplates() {
        return markTemplates;
    }

    public ArrayList<WaypointTemplateDTO> getWaypointTemplates() {
        return waypointTemplates;
    }

    public HashMap<MarkTemplateDTO, UUID> getAssociatedRoles() {
        return associatedRoles;
    }

    public Optional<String> getOptionalImageUrl() {
        return Optional.ofNullable(optionalImageUrl);
    }
    
    public ArrayList<String> getTags() {
        return tags;
    }

    public RepeatablePartDTO getRepeatablePart() {
        return repeatablePart;
    }

    public Integer getDefaultNumberOfLaps() {
        return defaultNumberOfLaps;
    }
}

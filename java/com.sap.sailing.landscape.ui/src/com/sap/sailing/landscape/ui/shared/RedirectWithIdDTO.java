package com.sap.sailing.landscape.ui.shared;

import java.util.UUID;
import java.util.function.Function;

/**
 * A redirect that is parameterized by a {@link UUID}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class RedirectWithIdDTO extends HomeRedirectDTO {
    private UUID id;
    
    @Deprecated
    RedirectWithIdDTO() {} // for GWT RPC only
    
    public RedirectWithIdDTO(UUID id, Type type) {
        super(type);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
    
    protected static <T extends RedirectWithIdDTO> T parse(String redirectPath, String queryPrefix, Function<UUID, T> constructor) {
        T result;
        final String query = RedirectDTO.getQuery(redirectPath);
        if (query.startsWith(queryPrefix)) {
            final String uuidCandidate = query.substring(queryPrefix.length());
            try {
                final UUID uuid = UUID.fromString(uuidCandidate);
                final T candidateWithUuid = constructor.apply(uuid);
                if (candidateWithUuid.getPathAndQuery().equals(redirectPath)) {
                    result = candidateWithUuid;
                } else {
                    result = null;
                }
            } catch (Exception invalidUuid) {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

}

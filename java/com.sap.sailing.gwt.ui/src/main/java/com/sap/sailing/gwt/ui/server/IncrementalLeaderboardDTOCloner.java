package com.sap.sailing.gwt.ui.server;

import java.lang.reflect.Field;

import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;

/**
 * Uses reflection to clone all properties of a {@link LeaderboardDTO} into a new instance of type {@link IncrementalLeaderboardDTO}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IncrementalLeaderboardDTOCloner {

    public IncrementalLeaderboardDTO clone(LeaderboardDTO leaderboardDTO) throws IllegalArgumentException, IllegalAccessException {
        IncrementalLeaderboardDTO result = new IncrementalLeaderboardDTO(leaderboardDTO.getId());
        Class<?> c = leaderboardDTO.getClass();
        while (c != null) {
            for (Field field : leaderboardDTO.getClass().getFields()) {
                field.setAccessible(true);
                Object value = field.get(leaderboardDTO);
                field.set(result, value);
            }
            c = c.getSuperclass();
        }
        return result;
    }
    
}

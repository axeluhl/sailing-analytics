package com.sap.sailing.domain.coursetemplate;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.coursetemplate.impl.MarkRolePairImpl;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkRolePair extends ControlPointTemplate {
    MarkRole getLeft();

    MarkRole getRight();
    
    /**
     * In the context of one {@link CourseTemplate}, this helps to reuse {@link MarkRolePair} instances that are equal
     * to each other. The factory maintains a cache of {@link MarkRolePair} objects, and a lookup is performed if a pair
     * is {@link #create(String, String, MarkRole, MarkRole) requested}; if an equal mark role pair already exists in
     * the cache, it is returned. Otherwise, a new pair is created, entered into the cache and then returned.
     */
    public class MarkRolePairFactory {
        private final Map<MarkRolePair, MarkRolePair> markPairs = new HashMap<>();

        public MarkRolePair create(String name, String shortName, MarkRole left, MarkRole right) {
            // use name as short name if none is provided
            final MarkRolePair markRolePair = new MarkRolePairImpl(name,
                    shortName != null && !shortName.isEmpty() ? shortName : name, left, right);
            // usage of computeIfAbsent ensures recycling of identical MarkPairTemplate instances in the CourseTemplate
            return markPairs.computeIfAbsent(markRolePair, mrp -> mrp);
        }
    }
}

package com.sap.sailing.domain.coursetemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.coursetemplate.impl.MarkRolePairImpl;
import com.sap.sse.common.Util.Pair;

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
     * In the context of one {@link CourseTemplate}, this helps to reuse {@link MarkRolePair} instances that equal
     * to each other.
     */
    public class MarkRolePairFactory {
        private final Map<MarkRolePair, MarkRolePair> markPairs = new HashMap<>();

        public MarkRolePair create(Pair<String, String> nameAndShortName, List<MarkRole> resolvedMarkRoles) {
            return create(nameAndShortName.getA(), nameAndShortName.getB(), resolvedMarkRoles);
        }

        public MarkRolePair create(String name, String shortName, List<MarkRole> resolvedMarkRoles) {
            return create(name, shortName, resolvedMarkRoles.get(0), resolvedMarkRoles.get(1));
        }

        public MarkRolePair create(String name, String shortName, MarkRole left, MarkRole right) {
            // use name as short name if none is provided
            final MarkRolePair markPairTemplate = new MarkRolePairImpl(name,
                    shortName != null && !shortName.isEmpty() ? shortName : name, left, right);
            // usage of computeIfAbsent ensures recycling of identical MarkPairTemplate instances in the CourseTemplate
            return markPairs.computeIfAbsent(markPairTemplate, mpt -> mpt);
        }
    }
}

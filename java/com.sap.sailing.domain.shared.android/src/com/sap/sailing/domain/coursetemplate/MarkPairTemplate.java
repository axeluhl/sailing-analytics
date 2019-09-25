package com.sap.sailing.domain.coursetemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairTemplateImpl;
import com.sap.sse.common.Util.Pair;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPairTemplate extends ControlPointTemplate {
    MarkTemplate getLeft();

    MarkTemplate getRight();
    
    /**
     * In the context of one {@link CourseTemplate}, this helps to reuse {@link MarkPairTemplate} instances that equal
     * to each other.
     */
    public class MarkPairTemplateFactory {
        private final Map<MarkPairTemplate, MarkPairTemplate> markPairs = new HashMap<>();

        public MarkPairTemplate create(Pair<String, String> nameAndShortName,
                List<MarkTemplate> resolvedMarkTemplates) {
            return create(nameAndShortName.getA(), nameAndShortName.getB(), resolvedMarkTemplates);
        }

        public MarkPairTemplate create(String name, String shortName, List<MarkTemplate> resolvedMarkTemplates) {
            return create(name, shortName, resolvedMarkTemplates.get(0), resolvedMarkTemplates.get(1));
        }

        public MarkPairTemplate create(String name, String shortName, MarkTemplate left, MarkTemplate right) {
            // use name as short name if none is provided
            final MarkPairTemplate markPairTemplate = new MarkPairTemplateImpl(name,
                    shortName != null && !shortName.isEmpty() ? shortName : name, left, right);
            // usage of computeIfAbsent ensures recycling of identical MarkPairTemplate instances in the CourseTemplate
            return markPairs.computeIfAbsent(markPairTemplate, mpt -> mpt);
        }
    }
}

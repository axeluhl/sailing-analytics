package com.sap.sailing.domain.coursetemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairTemplateImpl;

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

        public MarkPairTemplate create(String name, List<MarkTemplate> resolvedMarkTemplates) {
            return create(name, resolvedMarkTemplates.get(0), resolvedMarkTemplates.get(1));
        }

        public MarkPairTemplate create(String name, MarkTemplate left, MarkTemplate right) {
            MarkPairTemplate markPairTemplate = new MarkPairTemplateImpl(name, left, right);
            // usage of putIfAbsent ensures recycling of identical MarkPairTemplate instances in the CourseTemplate
            return markPairs.putIfAbsent(markPairTemplate, markPairTemplate);
        }
    }
}

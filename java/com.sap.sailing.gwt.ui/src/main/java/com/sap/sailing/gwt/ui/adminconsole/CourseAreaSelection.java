package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Visualizes a number of {@link CourseAreaDTO}s, together with the possibility to select
 * zero or more from those.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CourseAreaSelection extends CaptionPanel {
    private final Map<CourseAreaDTO, CheckBox> checkboxes;
    private final Set<CourseAreaDTO> selectedSet;
    
    public CourseAreaSelection(StringMessages stringMessages) {
        this(Collections.emptySet(), stringMessages);
    }
    
    public CourseAreaSelection(Iterable<CourseAreaDTO> courseAreas, StringMessages stringMessages) {
        super(stringMessages.selectCourseArea());
        checkboxes = new HashMap<>();
        selectedSet = new HashSet<>();
        for (final CourseAreaDTO courseArea : courseAreas) {
            addCourseArea(courseArea);
        }
    }

    public void addCourseArea(final CourseAreaDTO courseArea) {
        final CheckBox checkbox = new CheckBox(courseArea.getName());
        checkboxes.put(courseArea, checkbox);
        checkbox.addValueChangeHandler(e->{
            setSelected(courseArea, e.getValue());
        });
        add(checkbox);
    }
    
    /**
     * Only has an effect if {@code courseArea} has previously been {@link #addCourseArea(CourseAreaDTO) added} or has been passed
     * to the constructor for addition.
     */
    public void setSelected(CourseAreaDTO courseArea, boolean selected) {
        final CheckBox checkbox = checkboxes.get(courseArea);
        if (checkbox != null) {
            if (selected) {
                selectedSet.add(courseArea);
            } else {
                selectedSet.remove(courseArea);
            }
            checkbox.setValue(selected);
        }
    }
    
    public Iterable<CourseAreaDTO> getSelectedCourseAreas() {
        return selectedSet;
    }

    public void setEnabled(boolean b) {
        for (final CheckBox checkbox : checkboxes.values()) {
            checkbox.setEnabled(b);
        }
    }

    public void removeAll() {
        for (final CourseAreaDTO courseArea : new ArrayList<>(checkboxes.keySet())) {
            final CheckBox checkbox = checkboxes.remove(courseArea);
            remove(checkbox);
        }
        selectedSet.clear();
    }

    public void setSelectedSet(Iterable<CourseAreaDTO> selectedCourseAreas) {
        selectedSet.clear();
        for (final CourseAreaDTO selected : selectedCourseAreas) {
            setSelected(selected, true);
        }
    }
}

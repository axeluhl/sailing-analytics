package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Visualizes a number of {@link CourseAreaDTO}s, together with the possibility to select
 * zero or more from those.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CourseAreaSelection extends CaptionPanel {
    private final VerticalPanel checkboxPanel;
    private final Map<CourseAreaDTO, CheckBox> checkboxes;
    private final Set<CourseAreaDTO> selectedSet;
    private final Function<String, CheckBox> checkboxConstructor;
    
    public CourseAreaSelection(StringMessages stringMessages) {
        this(Collections.emptySet(), stringMessages, CheckBox::new);
    }
    
    public CourseAreaSelection(StringMessages stringMessages, DataEntryDialog<?> dialog) {
        this(Collections.emptySet(), stringMessages, dialog::createCheckbox);
    }
    
    public CourseAreaSelection(Iterable<CourseAreaDTO> courseAreas, StringMessages stringMessages,
            Function<String, CheckBox> checkboxConstructor) {
        super(stringMessages.selectCourseArea());
        checkboxPanel = new VerticalPanel();
        checkboxes = new HashMap<>();
        selectedSet = new HashSet<>();
        this.checkboxConstructor = checkboxConstructor;
        add(checkboxPanel);
        for (final CourseAreaDTO courseArea : courseAreas) {
            addCourseArea(courseArea);
        }
    }

    public void addCourseArea(final CourseAreaDTO courseArea) {
        final CheckBox checkbox = checkboxConstructor.apply(courseArea.getName());
        checkboxes.put(courseArea, checkbox);
        checkbox.addValueChangeHandler(e->{
            setSelected(courseArea, e.getValue());
        });
        checkboxPanel.add(checkbox);
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
            checkboxPanel.remove(checkbox);
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

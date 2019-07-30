package com.sap.sailing.server.impl;

import java.util.List;
import java.util.function.Predicate;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplateMapping;
import com.sap.sailing.domain.coursetemplate.CourseWithMarkTemplateMappings;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateMapping;
import com.sap.sailing.server.interfaces.CourseAndMarkMappingFactory;

public class CourseAndMarkMappingFactoryImpl implements CourseAndMarkMappingFactory {

    @Override
    public Course createCourse(CourseTemplate courseTemplate, int numberOfLaps) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps) {
        // TODO Auto-generated method stub

    }

    @Override
    public CourseTemplateMapping createMappingForCourseTemplate(Regatta regatta, CourseTemplate courseTemplate,
            Predicate<MarkProperties> markPropertiesFilter, Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseWithMarkTemplateMappings createCourseTemplateMappingFromMapping(Regatta regatta,
            CourseTemplateMapping courseTemplateMapping, int numberOfLaps) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Course createCourseFromMappingAndDefineMarksAsNeeded(Regatta regatta,
            CourseWithMarkTemplateMappings courseTemplateMappingWithMarkTemplateMappings) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<MarkTemplateMapping> createSuggestionsForMarkTemplate(Regatta regatta, MarkTemplate markTemplate,
            Predicate<MarkProperties> markPropertiesFilter, Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public CourseTemplate resolveCourseTemplate(Course course) {
        // TODO Auto-generated method stub
        return null;
    }

}

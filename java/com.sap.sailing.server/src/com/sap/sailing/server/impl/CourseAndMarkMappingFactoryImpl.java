package com.sap.sailing.server.impl;

import java.util.List;
import java.util.function.Predicate;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplateConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.server.interfaces.CourseAndMarkMappingFactory;
import com.sap.sse.common.TimePoint;

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
    public CourseTemplateConfiguration createMappingForCourseTemplate(Regatta regatta, CourseTemplate courseTemplate,
            Predicate<MarkProperties> markPropertiesFilter, Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Course createCourseFromMappingAndDefineMarksAsNeeded(Regatta regatta,
            CourseWithMarkConfiguration courseTemplateMappingWithMarkTemplateMappings,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Course createCourseFromMappingAndDefineMarksAsNeededAndExportCourseTemplate(Regatta regatta,
            CourseWithMarkConfiguration courseTemplateMappingWithMarkTemplateMappings,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings, String courseTemplateName) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<MarkConfiguration> createSuggestionsForMarkTemplate(Regatta regatta, MarkTemplate markTemplate,
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

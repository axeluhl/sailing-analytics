package com.sap.sailing.server.impl;

import java.util.List;
import java.util.function.Predicate;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
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
    public CourseTemplate resolveCourseTemplate(Course course) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkTemplate getOrCreateMarkTemplate(MarkConfiguration markConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseWithMarkConfiguration createCourseTemplateAndUpdatedConfiguration(String name,
            CourseWithMarkConfiguration courseWithMarkConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseTemplateConfiguration createMappingForCourseTemplate(Regatta regatta, CourseTemplate courseTemplate,
            Predicate<MarkProperties> markPropertiesFilter, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MarkConfiguration> createSuggestionsForMarkTemplate(Regatta regatta, MarkTemplate markTemplate,
            Predicate<MarkProperties> markPropertiesFilter, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseBase createCourseFromMappingAndDefineMarksAsNeeded(Regatta regatta,
            CourseWithMarkConfiguration courseTemplateMappingWithMarkTemplateMappings, int lapCount,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings, AbstractLogEventAuthor author) {
        // TODO Auto-generated method stub
        return null;
    }

}

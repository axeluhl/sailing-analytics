package com.sap.sailing.server.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.server.interfaces.CourseAndMarkConfigurationFactory;
import com.sap.sse.common.TimePoint;

public class CourseAndMarkConfigurationFactoryImpl implements CourseAndMarkConfigurationFactory {

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
    public CourseConfiguration createCourseTemplateAndUpdatedConfiguration(String name,
            CourseConfiguration courseWithMarkConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            CourseConfiguration courseTemplateMappingWithMarkTemplateMappings, int lapCount,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings, AbstractLogEventAuthor author) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPropertiesBasedMarkConfiguration createOrUpdateMarkProperties(MarkConfiguration markProperties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RegattaMarkConfiguration createMark(Regatta regatta, MarkConfiguration markConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate,
            Regatta optionalRegatta, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MarkProperties> createMarkPropertiesSuggestionsForMarkConfiguration(Regatta optionalRegatta,
            MarkConfiguration markConfiguration, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }

}

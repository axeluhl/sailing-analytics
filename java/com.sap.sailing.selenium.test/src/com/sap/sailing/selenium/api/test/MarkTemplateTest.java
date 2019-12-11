package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SHARED_SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplateApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MarkTemplateTest extends AbstractSeleniumTest {

    private static final String MARK_TEMPLATE_NAME = "testname";
    private static final String MARK_TEMPLATE_SHORTNAME = "testshortname";
    private static final String MARK_TEMPLATE_COLOR = "#FF0000";
    private static final String MARK_TEMPLATE_SHAPE = "shape";
    private static final String MARK_TEMPLATE_PATTERN = "pattern";
    private static final String MARK_TEMPLATE_TYPE = "STARTBOAT";

    private final MarkTemplateApi markTemplateApi = new MarkTemplateApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void createMarkTemplateWithDeviceUuidTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SHARED_SERVER_CONTEXT);
        MarkTemplate markTemplate = markTemplateApi.createMarkTemplate(ctx, MARK_TEMPLATE_NAME, MARK_TEMPLATE_SHORTNAME,
                MARK_TEMPLATE_COLOR, MARK_TEMPLATE_SHAPE, MARK_TEMPLATE_PATTERN, MARK_TEMPLATE_TYPE);
        assertNotNull("read: no MarkTemplate returnded", markTemplate);
        assertDefaultValues(markTemplate);
    }

    private void assertDefaultValues(MarkTemplate markTemplate) {
        assertNotNull("read: MarkTemplate.id is missing", markTemplate.getId());
        assertEquals("read: MarkTemplate.name is different", MARK_TEMPLATE_NAME, markTemplate.getName());
        assertEquals("read: MarkTemplate.shortName is different", MARK_TEMPLATE_SHORTNAME, markTemplate.getShortName());
        assertEquals("read: MarkTemplate.color is different", MARK_TEMPLATE_COLOR, markTemplate.getColor());
        assertEquals("read: MarkTemplate.shape is different", MARK_TEMPLATE_SHAPE, markTemplate.getShape());
        assertEquals("read: MarkTemplate.pattern is different", MARK_TEMPLATE_PATTERN, markTemplate.getPattern());
        assertEquals("read: MarkTemplate.type is different", MARK_TEMPLATE_TYPE, markTemplate.getMarkType().name());
    }

    @Test
    public void createAndGetMarkTemplateTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SHARED_SERVER_CONTEXT);
        MarkTemplate createdMarkTemplate = markTemplateApi.createMarkTemplate(ctx, MARK_TEMPLATE_NAME,
                MARK_TEMPLATE_SHORTNAME, MARK_TEMPLATE_COLOR, MARK_TEMPLATE_SHAPE, MARK_TEMPLATE_PATTERN,
                MARK_TEMPLATE_TYPE);
        MarkTemplate foundMarkTemplate = markTemplateApi.getMarkTemplate(ctx, createdMarkTemplate.getId());
        assertDefaultValues(foundMarkTemplate);
    }

    @Ignore
    public void createSeveralMarkTemplateAndGetAllTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SHARED_SERVER_CONTEXT);
        MarkTemplate markTemplate1 = markTemplateApi.createMarkTemplate(ctx, MARK_TEMPLATE_NAME,
                MARK_TEMPLATE_SHORTNAME, MARK_TEMPLATE_COLOR, MARK_TEMPLATE_SHAPE, MARK_TEMPLATE_PATTERN,
                MARK_TEMPLATE_TYPE);
        MarkTemplate markTemplate2 = markTemplateApi.createMarkTemplate(ctx, MARK_TEMPLATE_NAME,
                MARK_TEMPLATE_SHORTNAME, MARK_TEMPLATE_COLOR, MARK_TEMPLATE_SHAPE, MARK_TEMPLATE_PATTERN,
                MARK_TEMPLATE_TYPE);

        final Iterable<MarkTemplate> markTemplateResult = markTemplateApi.getAllMarkTemplates(ctx);
        for (MarkTemplate markTemplate : markTemplateResult) {
            System.out.println(markTemplate.getId());
            assertTrue(markTemplate.getId().equals(markTemplate1.getId())
                    || markTemplate.getId().equals(markTemplate2.getId()));
        }
    }
}

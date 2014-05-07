package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.CompetitorChangeListener;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.WithNationality;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;

/**
 * Tests the {@link CompetitorChangeListener} stuff including the serialization of the transitive listener
 * pattern on the {@link Team} structure.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CompetitorListenerTest extends AbstractSerializationTest {
    private CompetitorChangeListener listener;
    private boolean nationalityChanged;
    private Object whatChangedNationality;
    private Object oldNationality;
    private Object newNationality;
    private boolean sailIdChanged;
    private String oldSailId;
    private String newSailId;
    private boolean nameChanged;
    private String oldName;
    private String newName;
    private boolean colorChanged;
    private Color oldColor;
    private Color newColor;
    private DynamicCompetitor competitor;
    
    @Before
    public void setUp() {
        competitor = TrackBasedTest.createCompetitor("Hasso");
        nationalityChanged = false;
        whatChangedNationality = null;
        oldNationality = null;
        newNationality = null;
        sailIdChanged = false;
        oldSailId = null;
        newSailId = null;
        nameChanged = false;
        oldName = null;
        newName = null;
        colorChanged = false;
        oldColor = null;
        newColor = null;
        listener = new CompetitorChangeListener() {
            private static final long serialVersionUID = 4581029778988240209L;

            @Override
            public void nationalityChanged(WithNationality what, Nationality oldNationality, Nationality newNationality) {
                nationalityChanged = true;
                whatChangedNationality = what;
                CompetitorListenerTest.this.oldNationality = oldNationality;
                CompetitorListenerTest.this.newNationality = newNationality;
            }
            
            @Override
            public void sailIdChanged(String oldSailId, String newSailId) {
                sailIdChanged = true;
                CompetitorListenerTest.this.oldSailId = oldSailId;
                CompetitorListenerTest.this.newSailId = newSailId;
            }
            
            @Override
            public void nameChanged(String oldName, String newName) {
                nameChanged = true;
                CompetitorListenerTest.this.oldName = oldName;
                CompetitorListenerTest.this.newName = newName;
            }
            
            @Override
            public void colorChanged(Color oldColor, Color newColor) {
                colorChanged = true;
                CompetitorListenerTest.this.oldColor = oldColor;
                CompetitorListenerTest.this.newColor = newColor;
            }
        };
        competitor.addCompetitorChangeListener(listener);
    }
    
    @Test
    public void testSimpleCompetitorListenerPatternForName() {
        competitor.setName("Dr. Hasso Plattner");
        assertTrue(nameChanged);
        assertEquals("Hasso", oldName);
        assertEquals("Dr. Hasso Plattner", newName);
        assertFalse(sailIdChanged);
        assertFalse(colorChanged);
        assertFalse(nationalityChanged);
    }

    @Test
    public void testSimpleCompetitorListenerPatternForSailId() {
        final String myOldSailId = competitor.getBoat().getSailID();
        competitor.getBoat().setSailId("POR 40");
        assertFalse(nameChanged);
        assertTrue(sailIdChanged);
        assertEquals(myOldSailId, oldSailId);
        assertEquals("POR 40", newSailId);
        assertFalse(colorChanged);
        assertFalse(nationalityChanged);
    }

    @Test
    public void testSimpleCompetitorListenerPatternForColor() {
        final Color myOldColor = competitor.getColor();
        final RGBColor myNewColor = new RGBColor(123, 12, 234);
        competitor.setColor(myNewColor);
        assertFalse(nameChanged);
        assertFalse(sailIdChanged);
        assertTrue(colorChanged);
        assertEquals(myOldColor, oldColor);
        assertEquals(myNewColor, newColor);
        assertFalse(nationalityChanged);
    }

    @Test
    public void testSimpleCompetitorListenerPatternForNationality() {
        final Nationality myOldNationality = competitor.getTeam().getNationality();
        final Nationality myNewNationality = DomainFactory.INSTANCE.getOrCreateNationality("POR");
        competitor.getTeam().setNationality(myNewNationality);
        assertFalse(nameChanged);
        assertFalse(sailIdChanged);
        assertFalse(colorChanged);
        assertTrue(nationalityChanged);
        assertEquals(myOldNationality, oldNationality);
        assertEquals(myNewNationality, newNationality);
        assertSame(competitor.getTeam(), whatChangedNationality);
    }
}

package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;

import junit.framework.Assert;

public class PairingListTemplateTest extends PairingListTemplateImpl{

    @Test
    public void testPairingListCreationForValidValues() {
        this.createPairingListTemplate(15, 3, 18);

        int[][] plTemplate = this.getPairingListTemplate();

        Assert.assertNotNull(plTemplate);
        for(int[] i:plTemplate){
            for (int z: i){
                if(z<=0) Assert.fail("Values of Pairing List Template must not be 0!");
            }
        }
    }

    @Test
    public void testArrayCopy() {

        int[][] flightColumn={
                {1,2,3,4,5,6},
                {7,8,9,10,11,12},
                {13,14,15,16,17,18}
        };

        int[][][] associationRow=new int[3][5][18];
        int[][] currentAssociations={
                {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        };

        this.copyInto3rdDimension(18, currentAssociations, associationRow, flightColumn, 1,0);
        assertArrayEquals(currentAssociations[0],associationRow[0][0]);
        this.copyInto3rdDimension(18, currentAssociations, associationRow, flightColumn, 1,1);
        assertArrayEquals(currentAssociations[6],associationRow[1][0]);
    }

    @Test 
    public void testTeamAssociationCreation() {
        this.createPairingListTemplate(15, 3, 18);
        
        int[][] plTemplate = this.getPairingListTemplate();
        int[][] associations = new int[18][18];

        this.getAssociationsFromPairingList(plTemplate, associations);

        for (int x = 0; x < associations.length; x++) {
            for (int y = 0; y < associations[0].length; y++) {
                if ((x == y) && (associations[x][y] != -1)) {
                    Assert.fail("The diagonal of association matrix has to be -1.");
                }
            }
        }
    }

    @Test
    public void qualityCheck(){
        this.createPairingListTemplate(15, 3, 18);
        if(getQuality()>=0.7) {
            Assert.fail("Quality of Pairinglist is worse than usual!");
        }
        
        this.createPairingListTemplate(10, 3, 30);
        if(getQuality()>=1.2) {
            Assert.fail("Quality of Pairinglist is worse than usual!");
        }
    }

    @Test
    public void testAverageTimeForSingleCase(){
        final int tests = 5;
        long[] a=new long[tests]; 
        
        for(int i = 0;i < tests; i++){
            long time = System.currentTimeMillis();
            
            this.createPairingListTemplate(15, 3, 18);
            
            time = System.currentTimeMillis() - time;
            a[i] = time;
        }
        
        long sum = 0;
        for(int i = 0; i < tests; i++) sum += a[i];
        double average = sum/tests;
        if (average > 8000) {
            Assert.fail("The calculation of Pairing Lists took longer than expected!");
        }
    }
    @Test
    public void testAssignmentQuality(){
        this.createPairingListTemplate(15, 3, 18);
        if(calcStandardDev(getAssignmentAssociations(this.getPairingListTemplate(), new int[18][6]))>=1.5) {
            Assert.fail("Quality of Boat Assignments is worse than usual!");
        }
        
        this.createPairingListTemplate(10, 3, 30);
        if(calcStandardDev(getAssignmentAssociations(this.getPairingListTemplate(), new int[30][10]))>=0.6) {
            Assert.fail("Quality of Boat Assignments is worse than usual!");
        }
    }
}

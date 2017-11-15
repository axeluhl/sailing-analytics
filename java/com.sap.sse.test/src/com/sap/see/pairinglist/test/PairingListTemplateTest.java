package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;
import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;

import junit.framework.Assert;

public class PairingListTemplateTest extends PairingListTemplateImpl{


    public PairingListTemplateTest() {
        super(new PairingFrameProviderTest(15, 3, 18));
    }

    PairingListTemplateFactoryImpl factory;
    

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
        final int flights = 15;
        final int groups = 3;
        final int competitors = 18;
        
        factory = new PairingListTemplateFactoryImpl();
        
        int[][] plTemplate = factory.getOrCreatePairingListTemplate(new PairingFrameProviderTest(flights, groups, competitors)).getPairingListTemplate();
        int[][] associations = new int[competitors][competitors];

        this.getAssociationsFromPairingList(plTemplate, associations);

        for (int x = 0; x < associations.length; x++) {
            for (int y = 0; y < associations[0].length; y++) {
                if ((x == y) && (associations[x][y] != -1)) {
                    Assert.fail("The diagonal of association matrix has to be -1.");
                }
                if(associations[x][y]>flights&&(associations[x][y]<-1)){
                    Assert.fail("Calculation of assosciation matrix failed!");
                }
            }
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
    public void testStandardDevCalc(){
        int[][] givenPairingList={
                {18,15,17,14,16,8},
                {13,1,10,9,12,11},
                {3,5,2,4,6,7},
                {18,13,17,4,12,7},
                {11,15,16,6,1,5},
                {14,10,8,2,9,3},
                {15,18,11,2,13,3},
                {17,10,14,6,7,1},
                {8,4,16,5,9,12},
                {7,3,16,1,9,18},
                {12,15,10,17,5,2},
                {6,11,4,13,8,14},
                {16,11,4,17,3,10},
                {12,2,6,8,18,1},
                {7,13,5,15,14,9},
                {1,3,12,15,14,4},
                {9,17,6,16,13,2},
                {5,18,7,11,10,8},
                {16,2,7,11,14,12},
                {3,1,5,8,17,13},
                {6,9,4,10,15,18},
                {14,16,13,10,5,18},
                {11,2,1,9,4,17},
                {8,12,3,7,6,15},
                {10,13,8,7,16,15},
                {2,14,18,4,5,1},
                {6,17,9,12,11,3},
                {14,17,18,12,11,5},
                {15,16,13,1,4,2},
                {8,6,3,10,7,9},
                {16,6,12,18,2,10},
                {1,8,15,14,17,9},
                {5,7,13,3,4,11},
                {9,7,18,2,15,11},
                {13,3,14,1,12,10},
                {17,4,5,16,8,6},
                {17,4,12,15,10,7},
                {3,1,11,18,8,16},
                {2,9,6,5,13,14},
                {18,9,17,5,3,15},
                {12,7,1,13,16,6},
                {10,14,2,8,11,4},
                {7,14,2,17,3,16},
                {15,5,10,11,1,6},
                {4,12,8,9,18,13}
                                 };
        System.out.println(calcStandardDev(getAssociationsFromPairingList(givenPairingList, new int[18][18])));
        }
    }
    
   


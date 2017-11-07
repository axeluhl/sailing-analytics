package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertArrayEquals;  
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;
import junit.framework.Assert;

public class PairingListTemplateTest extends PairingListTemplateImpl{
   
    @Before
    public void init() {
        create(15, 3, 18,10000); 
        // creating pairing list template

        this.create(15, 3, 18,10000);
    }
  
  
    @Test
    public void testPairingListCreation() {
        int[][] plTemplate = this.getPairingListTemplate();
        
        assertNotNull(plTemplate);
        for(int[] i:plTemplate){
            for (int z: i){
                if(z<=0) fail("Problem in .create!");
            }
        }
        System.out.println(this.calcStandardDev(this.getAssignmentAssociations(plTemplate, new int[18][18/3])));
        for(int x=0;x<100;x++){
            create(15, 3, 18,10000);
            plTemplate=this.getPairingListTemplate();
            System.out.println(this.calcStandardDev(this.getAssignmentAssociations(plTemplate, new int[18][18/3])));
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
        int[][] plTemplate = this.getPairingListTemplate();
        int[][] associations = new int[18][18];
        
        this.getAssociationsFromPairingList(plTemplate, associations);
        
        for (int x = 0; x < associations.length; x++) {
            for (int y = 0; y < associations[0].length; y++) {
                if ((x == y) && (associations[x][y] != -1)) {
                    Assert.fail("In 'getAssociationsFromPairingList' the diagonal of association matrix has to be -1.");
                }
            }
        }
    }
    @Test
    public void qualityCheck(){
        if(getQuality()>=0.7) fail("Quality of Pairinglist is too bad!");
        create(10, 3, 30,10000);
        if(getQuality()>=2) fail("Quality of Pairinglist is too bad!");
    }
}

package com.sap.see.pairinglist.test;

import java.security.acl.Group;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;

import junit.framework.Assert;


public class PairingListTest<Flight, Group, Competitor> {
    PairingListTemplateImpl<Flight, Group, Competitor> aImpl;
    
    @Before
    public void init() {
        this.aImpl = new PairingListTemplateImpl<Flight, Group, Competitor>();
        
        // creating pairing list template
        this.aImpl.create(15, 3, 18);
    }
  
  
    @Test
    public void testPairingListCreation() {
        int[][] plTemplate = this.aImpl.getPairingListTemplate();
        
        Assert.assertNotNull(plTemplate);
        for(int[] i:plTemplate){
            for (int z: i){
                if(z<=0) Assert.fail("Problem in .create!");
            }
        }
    }
   
    @Test 
    public void testTeamAssociationCreation() {
        int[][] plTemplate = this.aImpl.getPairingListTemplate();
        int[][] associations = new int[18][18];
        
        this.aImpl.getAssociationsFromPairingList(plTemplate, associations);
        
        for (int x = 0; x < associations.length; x++) {
            for (int y = 0; y < associations[0].length; y++) {
                if ((x == y) && (associations[x][y] != -1)) {
                    Assert.fail("In 'getAssociationsFromPairingList' the diagonal of association matrix has to be -1.");
                }
            }
        }
    }
}

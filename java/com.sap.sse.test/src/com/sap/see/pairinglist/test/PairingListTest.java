package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;


import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;


public class PairingListTest<Flight, Group, Competitor> {
    PairingListTemplateImpl<Flight, Group, Competitor> aImpl;
  
  
   @Test
   public void testPairingListCreation() {
           aImpl=new PairingListTemplateImpl<>();
           int[][] temp=aImpl.create(15, 3, 18);
           assertNotNull(temp);
           for(int[] i:temp){
               for (int z: i){
                   if(z<=0) fail("Problem in .create!");
               }
           }
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
                  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                                      };
          
           
           aImpl.copyInto3rdDimension(18, currentAssociations, associationRow, flightColumn, 1);
          
           assertArrayEquals(currentAssociations[0],associationRow[0][0]);
   }
   
}

package com.sap.see.pairinglist.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


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
   }
   
}

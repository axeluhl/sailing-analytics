package com.sap.sailing.gwt.ui.adminconsole;

import com.github.gwtbootstrap.client.ui.ProgressBar;

public class StructureImportProgressBar extends ProgressBar{
       double percent;
       
       public StructureImportProgressBar(int amountOfRegattas){
             this(amountOfRegattas, Style.DEFAULT);
             
       }
       public StructureImportProgressBar(int amountOfRegattas, Style style){
             super(style);
             percent = 100.0/amountOfRegattas;
             setPercent(0);
       }

       public void setPercent(int parsed) {           
             super.setPercent((int)(percent*parsed));
       }
       

       

}


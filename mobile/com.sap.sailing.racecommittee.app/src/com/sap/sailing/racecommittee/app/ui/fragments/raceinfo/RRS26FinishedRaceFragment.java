/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sap.sailing.racecommittee.app.R;


public class RRS26FinishedRaceFragment extends FinishedRaceFragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Button positioningButton = (Button) getView().findViewById(R.id.buttonPositioning);
        positioningButton.setVisibility(View.GONE);
    }
   
}

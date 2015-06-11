package com.sap.sailing.android.tracking.app.test;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.widget.Button;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.StartActivity;

public class StartActivityTest extends ActivityUnitTestCase<StartActivity> {
	private Intent mStartIntent;
	private FragmentManager fragmentManager;
    
	public StartActivityTest() {
		super(StartActivity.class);
	}
	
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();        
        
        ContextThemeWrapper context = new ContextThemeWrapper(
				getInstrumentation().getTargetContext(), R.style.AppTheme);
		setActivityContext(context);
		
		mStartIntent = new Intent(Intent.ACTION_MAIN);
		startActivity(mStartIntent, null, null);
	    fragmentManager = getActivity().getSupportFragmentManager();
    }
	
	public void testHasTwoButtons() {
     
        getInstrumentation().callActivityOnStart(getActivity());
        getInstrumentation().callActivityOnResume(getActivity());
        getActivity().getFragmentManager().executePendingTransactions();
        
        fragmentManager.executePendingTransactions();
        
        Button scanButton = (Button) getActivity().findViewById(R.id.scanQr);
        Button noQRButton = (Button) getActivity().findViewById(R.id.noQrCode);
        
        assertNotNull(getActivity());
        assertNotNull(scanButton);
        assertNotNull(noQRButton);
    }
	
	
}

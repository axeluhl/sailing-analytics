package com.sap.sailing.android.shared.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.util.CheckinDataHandler;

public abstract class CheckinDataActivity extends AbstractBaseActivity implements
		CheckinDataHandler {

	@Override
	public abstract void onCheckinDataAvailable(AbstractCheckinData data);

	/**
	 * Shows a pop-up-dialog that informs the user than an DB-operation has
	 * failed.
	 */
	public void displayDatabaseError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.notify_user_db_operation_failed));
		builder.setCancelable(true);
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}

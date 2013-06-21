package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

public interface DialogListenerHost {
    public interface DialogResultListener {
        void onDialogNegativeButton(BaseDialogFragment dialog);
        void onDialogPositiveButton(BaseDialogFragment dialog);
    }

    DialogResultListener getListener();
}

package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

public interface DialogListenerHost {
    public interface DialogResultListener {
        void onDialogNegativeButton(AttachedDialogFragment dialog);

        void onDialogPositiveButton(AttachedDialogFragment dialog);
    }

    DialogResultListener getListener();
}

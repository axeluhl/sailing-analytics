package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class BackAwareEditText extends EditText{
    private InputDownPressedListener mInputDownPressedListener;

    public BackAwareEditText(Context context)
    {
        super(context);
        init();
    }

    public BackAwareEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public BackAwareEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {

    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mInputDownPressedListener != null) mInputDownPressedListener.onImeBack(this);
        }
        return super.dispatchKeyEvent(event);
    }

    public void setInputDownPressedListener(InputDownPressedListener listener) {
        mInputDownPressedListener = listener;
    }

    public interface InputDownPressedListener {
        void onImeBack(BackAwareEditText editText);
    }
}

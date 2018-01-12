package com.sap.sailing.android.tracking.app.customviews;

import com.sap.sailing.android.tracking.app.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Draws one vertical indicator that can is filled green up to a point, 
 * depending on the value passed for signal-quality;
 * 
 * @author Lukas Zielinski
 *
 */
public class SignalQualityIndicatorView extends View {

	private int signalQuality;
	
	private Paint paint; 
	private Paint paintDark;
	
	private float height;
	private float width;
	private Rect rect;
	
	public SignalQualityIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SignalQualityIndicatorView, 0, 0);
		try {
			signalQuality = a.getInteger(R.styleable.SignalQualityIndicatorView_signalQuality, 0);
		} finally {
			a.recycle();
		}
		
		setAccessibilityString();
		initPaint();
	}
	
	public Integer getSignalQuality() {
		return signalQuality;
	}
	
	private void setAccessibilityString()
	{
		if (this.signalQuality == 2)
		{
			this.setContentDescription(getContext().getString(R.string.signal_accuracy_indicator_view_description) + getContext().getString(R.string.poor));
		}
		else if (this.signalQuality == 3)
		{
			this.setContentDescription(getContext().getString(R.string.signal_accuracy_indicator_view_description) + getContext().getString(R.string.good));
		}
		else if (this.signalQuality == 4)
		{
			this.setContentDescription(getContext().getString(R.string.signal_accuracy_indicator_view_description) + getContext().getString(R.string.great));
		}
		else
		{
			this.setContentDescription(getContext().getString(R.string.signal_accuracy_indicator_view_description) + getContext().getString(R.string.no_signal));
		}
	}

	private void initPaint() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(getContext().getResources().getColor(R.color.signal_great));
		paintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintDark.setColor(getContext().getResources().getColor(R.color.signal_none));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float boxHeight = this.height / 4;
		
		// first draw gray background
		
		rect.top = 0;
		rect.left = 0;
		rect.right = (int) this.width;
		rect.bottom = (int) this.height;
		canvas.drawRect(rect, paintDark);

		// then draw green box on top
		
		rect.top = (int) (this.height - (signalQuality * boxHeight));
		rect.left = 0;
		rect.right = (int) this.width;
		rect.bottom = (int) this.height;
		canvas.drawRect(rect, paint);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		float boxWidth = w / 4.5f;
		float boxHeight = h;
		
		rect = new Rect(0, 0, (int)boxWidth, (int)boxHeight);
		
		float xpad = (float)(getPaddingLeft() + getPaddingRight());
	    float ypad = (float)(getPaddingTop() + getPaddingBottom());
	    this.width = (float)w - xpad;
	    this.height = (float)h - ypad;
	}
	
	@Override
	public int getBaseline() {
		return getMeasuredHeight() - 10;
	}

	/**
	 * Must be 0,2,3 or 4, otherwise 1 will be set
	 * 
	 * @param signalQuality
	 */
	public void setSignalQuality(Integer signalQuality) {
		Integer previousSingalQuality = this.signalQuality;
		
		if (signalQuality == 0) {
			this.signalQuality = 0;
		} else if (signalQuality == 2) {
			this.signalQuality = 2;
		} else if (signalQuality == 3) {
			this.signalQuality = 3;
		} else if (signalQuality == 4) {
			this.signalQuality = 4;
		} else {
			this.signalQuality = 0;
		}

		if (this.signalQuality != previousSingalQuality)
		{
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
		}
		
		setAccessibilityString();
		
		invalidate();
		requestLayout();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	   int minw = getPaddingLeft() + getPaddingRight() + 20;
	   int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
	   int minh = 50;
	   setMeasuredDimension(w, minh);
	}
	
	/**
	 * generate some accessibility info
	 */
	public String getAccessibilityText()
	{
		if (this.signalQuality == 2) return "poor signal quality";
		if (this.signalQuality == 3) return "good signal quality";
		if (this.signalQuality == 4) return "great signal quality";
		return "no signal";
	}
	
	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		
		CharSequence text = getAccessibilityText();
        if (!TextUtils.isEmpty(text)) {
            event.getText().add(text);
        }
	}
}

package co.foodcircles.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout
{
	public SquareFrameLayout(Context context)
	{
		super(context);
	}

	public SquareFrameLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.N_MR1)
	public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		
	}
}

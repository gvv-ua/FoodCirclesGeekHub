package co.foodcircles.customviews;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class CenterTopImageView extends AppCompatImageView
{

	public CenterTopImageView(Context context)
	{
		super(context);
		setup();
	}

	public CenterTopImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setup();
	}

	public CenterTopImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setup();
	}

	private void setup()
	{
		setScaleType(ScaleType.MATRIX);
	}

	@Override
	protected boolean setFrame(int frameLeft, int frameTop, int frameRight, int frameBottom)
	{
		float frameWidth = frameRight - frameLeft;
		float frameHeight = frameBottom - frameTop;

		float originalImageWidth = (float) getDrawable().getIntrinsicWidth();
		float originalImageHeight = (float) getDrawable().getIntrinsicHeight();

		float usedScaleFactor = 1;
		float fitHorizontallyScaleFactor = frameWidth / originalImageWidth;
		float fitVerticallyScaleFactor = frameHeight / originalImageHeight;

		usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);

		Matrix matrix = getImageMatrix();
		matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0);
		setImageMatrix(matrix);
		return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
	}
}

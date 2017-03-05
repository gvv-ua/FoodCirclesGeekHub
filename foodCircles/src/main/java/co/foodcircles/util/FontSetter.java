package co.foodcircles.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontSetter
{
	public static Typeface font;
	public static Typeface boldFont;
	public static Typeface italicFont;
	public static Typeface boldItalicFont;
	public static Typeface sysFont;
	public static Typeface sysBoldFont;
	public static Typeface sysItalicFont;
	public static Typeface sysBoldItalicFont;

	private static void initFonts(final Context context)
	{
		if (font == null)
		{
			font = Typeface.createFromAsset(context.getAssets(), "neutrafaceslabtextbook.ttf");
			boldFont = Typeface.createFromAsset(context.getAssets(), "neutrafaceslabtextbold.ttf");
			italicFont = Typeface.createFromAsset(context.getAssets(), "neutrafaceslabtextbookitalic.ttf");
			boldItalicFont = Typeface.createFromAsset(context.getAssets(), "neutrafaceslabtextbolditalic.ttf");

			sysFont = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
			sysBoldFont = Typeface.createFromAsset(context.getAssets(), "Roboto-Bold.ttf");
			sysItalicFont = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
			sysBoldItalicFont = Typeface.createFromAsset(context.getAssets(), "Roboto-BoldItalic.ttf");
		}
	}

	public static void overrideFonts(final Context context, final View v)
	{
		initFonts(context);
		try
		{
			if (v == null) return;
			if (v instanceof ViewGroup)
			{
				ViewGroup vg = (ViewGroup) v;
				for (int i = 0; i < vg.getChildCount(); i++)
				{
					View child = vg.getChildAt(i);
					overrideFonts(context, child);
				}
			}
			else if ((v instanceof TextView || (v.getClass().getSimpleName().equals("TabView"))))
			{
				TextView textView = (TextView) v;
				if ((v.getClass().getSimpleName().equals("TabView")) || ((v.getTag() != null) && ((String) v.getTag()).equals("neu")))
				{
					Typeface newFont = font;
					if (textView.getTypeface() != null)
					{
						if (textView.getTypeface().isBold())
						{
							newFont = (textView.getTypeface().isItalic()) ? boldItalicFont : boldFont;
						}
						else
						{
							if (textView.getTypeface().isItalic())
								newFont = italicFont;
						}
					}
					textView.setTypeface(newFont);
				}

				else if ((v.getTag() != null) && ((String) v.getTag()).equals("rob"))
				{
					Typeface newFont = sysFont;
					if (textView.getTypeface() != null)
					{
						if (textView.getTypeface().isBold())
						{
							newFont = (textView.getTypeface().isItalic()) ? boldItalicFont : boldFont;
						}
						else
						{
							if (textView.getTypeface().isItalic())
								newFont = sysItalicFont;
						}
					}
					textView.setTypeface(newFont);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}

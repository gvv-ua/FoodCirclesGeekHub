package co.foodcircles.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.foodcircles.R;
import co.foodcircles.util.TwitterLogin.TwDialogListener;


public class TwitterDialog extends Dialog {

    private static final float[] DIMENSIONS_LANDSCAPE = {480, 280};
    private static final float[] DIMENSIONS_PORTRAIT = {300, 450};
    private static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    private static final int MARGIN = 4;
    private static final int PADDING = 2;
    private final String url;
    private final TwDialogListener listener;
    private ProgressDialog spinner;
    private TextView title;

    public TwitterDialog(Context context, String url, TwDialogListener listener) {
        super(context);
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spinner = new ProgressDialog(getContext());
        spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spinner.setMessage("Loading...");

        setUpWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        WebView mWebView;
        mWebView = new WebView(getContext());
        LinearLayout mContent;
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable icon = getContext().getResources().getDrawable(R.drawable.ic_stat_logo);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        @SuppressWarnings("deprecation")
        float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT
                : DIMENSIONS_LANDSCAPE;
        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
                * scale + 0.5f)));
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new TwitterWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url);
        mWebView.setLayoutParams(FILL);
        title = new TextView(getContext());
        title.setText(R.string.app_name);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setBackgroundColor(0xFFbbd7e9);
        title.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        title.setCompoundDrawablePadding(MARGIN + PADDING);
        title.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        mContent.addView(title);
        mContent.addView(mWebView);
    }

    private class TwitterWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            if (url.startsWith(Const.CALLBACK_URL)) {
                listener.onComplete(url);
                TwitterDialog.this.dismiss();
                return true;
            } else if (url.startsWith("authorize")) {
                return false;
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            try {
                super.onReceivedError(view, errorCode, description, failingUrl);
                listener.onError(description);
                TwitterDialog.this.dismiss();
            } catch (NullPointerException e) {
            } catch (Exception e) {
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.startsWith(Const.CALLBACK_URL)) {
                listener.onComplete(url);
                TwitterDialog.this.dismiss();
            } else {
                spinner.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = view.getTitle();
            if (title != null && title.length() > 0) {
                TwitterDialog.this.title.setText(title);
            }
            spinner.dismiss();
        }
    }
}

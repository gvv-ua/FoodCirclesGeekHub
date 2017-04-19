package co.foodcircles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import co.foodcircles.R;
import co.foodcircles.net.Net;
import co.foodcircles.util.FacebookShare;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesUtils;

public class CarouselFragment extends Fragment {
    MixpanelAPI mixpanel;
    private FacebookShare facebookShare;

    @Override
    public void onStart() {
        super.onStart();
        mixpanel = MixpanelAPI.getInstance(getActivity(), getResources()
                .getString(R.string.mixpanel_token));
    }

    @Override
    public void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.carousel, null);
        FontSetter.overrideFonts(getActivity(), view);
        (view.findViewById(R.id.imageViewTop)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Net.HOST));
                startActivity(browserIntent);
            }
        });


        (view.findViewById(R.id.imageViewTwitter)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri uri = FoodCirclesUtils.getLogoUri(getActivity());
                TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                        .text("Savings with a Conscience! Snag a $1 dish and $1 donated to feed a hungry child! #bofo: http://www.joinfoodcircles.org @foodcircles");
                if (uri != null) {
                    builder.image(uri);
                }
                builder.show();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        facebookShare = (FacebookShare) getActivity();

        (view.findViewById(R.id.imageViewFacebook)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MP.track(mixpanel, "Shared Via Facebook", "activity", "News");
                facebookShare.shareOnFacebook("Savings with a Conscience! Snag a $1 dish and $1 donated to feed a hungry child! #bofo: http://www.joinfoodcircles.org @foodcircles");
            }
        });

    }
}

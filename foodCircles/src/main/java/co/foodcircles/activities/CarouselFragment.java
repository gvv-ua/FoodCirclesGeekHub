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

import co.foodcircles.R;
import co.foodcircles.net.Net;
import co.foodcircles.util.FacebookShare;
import co.foodcircles.util.FontSetter;

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
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/intent/tweet?source=webclient&text=Local+restaurants%2C+a+%241+dish%2C+and+%241+donated+to+feed+a+hungry+child.+Go+%23bofo%3A+http%3A%2F%2Fwww.joinfoodcircles.org%C2%A0+%40foodcircles"));
                    startActivity(intent);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/#!/FoodCircles")));
                }
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

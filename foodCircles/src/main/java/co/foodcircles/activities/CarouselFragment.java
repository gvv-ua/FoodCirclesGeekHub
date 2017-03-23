package co.foodcircles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import co.foodcircles.R;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;

//import com.sromku.simple.fb.Permission;
//import com.sromku.simple.fb.SimpleFacebook;
//import com.sromku.simple.fb.entities.Feed;
//import com.sromku.simple.fb.listeners.OnLoginListener;
//import com.sromku.simple.fb.listeners.OnPublishListener;

public class CarouselFragment extends Fragment {
	MixpanelAPI mixpanel;
//	SimpleFacebook mSimpleFacebook;
	
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
		((ImageView) view.findViewById(R.id.imageViewTop)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Net.HOST));
			startActivity(browserIntent);
			}
		});
		
		((ImageView) view.findViewById(R.id.imageViewFacebook)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MP.track(mixpanel, "Shared Via Facebook", "activity","News");
//				if (mSimpleFacebook.isLogin()){
//					mSimpleFacebook.publish(feed, true, onPublishListener);
//				} else {
//					mSimpleFacebook.login(mOnLoginListener);
//				}
			}	
		});

		((ImageView) view.findViewById(R.id.imageViewTwitter)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent( Intent.ACTION_VIEW,
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
	public void onResume() {
		super.onResume();
//		mSimpleFacebook = SimpleFacebook.getInstance(getActivity());
	}
	
//	private OnLoginListener mOnLoginListener = new OnLoginListener() {
//		@Override
//		public void onFail(String reason) {
//			Toast.makeText(getActivity().getBaseContext(), "Facebook Login Failed:" + reason, Toast.LENGTH_SHORT).show();
//		}
//
//		@Override
//		public void onException(Throwable throwable) {
//			Toast.makeText(getActivity().getBaseContext(), "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
//			throwable.printStackTrace();
//		}
//
//		@Override
//		public void onThinking() {
//			// Place here if we want to show progress bar while login is occurring
//		}
//
//		@Override
//		public void onLogin() {
//			mSimpleFacebook.publish(feed, true, onPublishListener);
//		}
//
//		@Override
//		public void onNotAcceptingPermissions(Permission.Type type) {
//			Toast.makeText(getActivity().getBaseContext(),"Facebook permissions cancelled!", Toast.LENGTH_SHORT).show();
//		}
//	};
//
//	private OnPublishListener onPublishListener = new OnPublishListener() {
//		@Override
//		public void onFail(String reason) {
//			Toast.makeText(getActivity().getBaseContext(),"Whoops! The post didn't go through!", Toast.LENGTH_SHORT).show();
//		}
//		@Override
//		public void onException(Throwable throwable) {
//			Toast.makeText(getActivity().getBaseContext(),"Whoops! The post didn't go through!", Toast.LENGTH_SHORT).show();
//		}
//		@Override
//		public void onThinking() {
//		}
//
//		@Override
//		public void onComplete(String postId) {
//			Toast.makeText(getActivity().getBaseContext(),"Thanks for sharing the word!", Toast.LENGTH_SHORT).show();
//		}
//	};
//
//	// feed builder
//	private Feed feed = new Feed.Builder()
//			.setMessage("Try FoodCircles!")
//			.setName("Savings with a Conscience!")
//			.setCaption("Snag a $1 dish and $1 donated to feed a hungry child!")
//			.setDescription("#bofo: http://www.joinfoodcircles.org @foodcircles ")
//			.setPicture(Net.logo).setLink("http://www.joinfoodcircles.org").build();
}

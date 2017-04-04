package co.foodcircles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import co.foodcircles.R;
import co.foodcircles.adapters.TimelineAdapter;
import co.foodcircles.data.ReservationList;
import co.foodcircles.json.Reservation;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

//import com.sromku.simple.fb.Permission;
//import com.sromku.simple.fb.SimpleFacebook;
//import com.sromku.simple.fb.entities.Feed;
//import com.sromku.simple.fb.listeners.OnLoginListener;
//import com.sromku.simple.fb.listeners.OnPublishListener;

public class TimelineFragment extends Fragment implements ReservationList.OnDataUpdateSuccessCallback, ReservationList.OnDataUpdateFailCallback{
    private TimelineAdapter adapter;
    private FoodCirclesApplication app;
    private MixpanelAPI mixpanel;
//    private SimpleFacebook mSimpleFacebook;
//    private Feed feed;

    @Override
    public void onStart() {
        super.onStart();
        mixpanel = MixpanelAPI.getInstance(getActivity(), getResources()
                .getString(R.string.mixpanel_token));
    }

    @Override
    public void onDestroy() {
        if (mixpanel != null)
            mixpanel.flush();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        app = (FoodCirclesApplication) getActivity().getApplicationContext();
        return inflater.inflate(R.layout.timeline_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FontSetter.overrideFonts(getActivity(), view);
        final String token = FoodCirclesUtils.getToken(getActivity());
        adapter = new TimelineAdapter(ReservationList.getInstance().getReservations(), new TimelineAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Reservation item) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
                Bundle args = new Bundle();
                args.putParcelable(ReceiptDialogFragment.CURRENT_RESERVATION, item);
                receiptDialog.setArguments(args);
                receiptDialog.show(fm, "receipt_dialog");
                MP.track(mixpanel, "Timeline", "Opened voucher");
            }
        });

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.rvList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        TextView inviteOrImportFriends = (TextView) view.findViewById(R.id.textViewInviteOrImport);

        if (FoodCirclesUtils.isConnectedToSocialAccounts(TimelineFragment.this.getActivity())) {
            inviteOrImportFriends.setText(R.string.invite_friends);
        }

        ImageView settingsButton = (ImageView) view.findViewById(R.id.settingButton);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountOptionsActivity.class);
                startActivity(intent);
            }
        });


        LinearLayout inviteFriends = (LinearLayout) view.findViewById(R.id.inviteFriendsLayout);
        inviteFriends.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				feed = new Feed.Builder()
//				.setMessage("Try FoodCircles!")
//				.setName("Savings with a Conscience!")
//				.setCaption("Local restaurants, a $1 dish, and $1 donated to feed a hungry child.")
//				.setDescription("#bofo: http://www.joinfoodcircles.org @foodcircles ")
//				.setPicture(Net.logo).setLink("http://www.joinfoodcircles.org").build();
//				if (mSimpleFacebook.isLogin()){
//					mSimpleFacebook.publish(feed, true, onPublishListener);
//				} else {
//					mSimpleFacebook.login(mOnLoginListener);
//				}
            }
        });

        ReservationList.getInstance().updateData(token, this, this);
    }

    @Override
    public void onUpdateVenuesSuccess() {
        TextView tvKidsFed = (TextView) getActivity().findViewById(R.id.textViewKidFed);
        tvKidsFed.setText(String.format("%d", ReservationList.getInstance().getTotalKidsFed()));

        if (ReservationList.getInstance().getReservations().size() == 0) {
            (getActivity().findViewById(R.id.noPurchases)).setVisibility(View.VISIBLE);
        }
        adapter.updateAdapter(ReservationList.getInstance().getReservations());
    }

    @Override
    public void onUpdateVenuesFailed() {
        MP.track(mixpanel, "Restaurant List", "Failed to load venues");
        //this means the list is empty!  If you'd like to display
        //any sort of indicator, here would be the place to do it.
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
}

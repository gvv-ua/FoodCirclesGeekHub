package co.foodcircles.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.adapters.TimelineAdapter;
import co.foodcircles.adapters.base.ViewItem;
import co.foodcircles.adapters.viewitems.TimelineExpiringVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineFriendViewItem;
import co.foodcircles.adapters.viewitems.TimelineHeaderViewItem;
import co.foodcircles.adapters.viewitems.TimelineMonthViewItem;
import co.foodcircles.adapters.viewitems.TimelineUsedVoucherViewItem;
import co.foodcircles.adapters.viewitems.TimelineVoucherViewItem;
import co.foodcircles.json.Reservation;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;
import co.foodcircles.util.TimelineHelper;

//import com.sromku.simple.fb.Permission;
//import com.sromku.simple.fb.SimpleFacebook;
//import com.sromku.simple.fb.entities.Feed;
//import com.sromku.simple.fb.listeners.OnLoginListener;
//import com.sromku.simple.fb.listeners.OnPublishListener;

public class TimelineFragment extends Fragment {
    private TimelineAdapter adapter;
    private FoodCirclesApplication app;
    private MixpanelAPI mixpanel;
//    private SimpleFacebook mSimpleFacebook;
//    private Feed feed;
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<ViewItem> items = new ArrayList<>();

    private static final int TIMELINE_YOU_AND_FRIENDS_TYPE = 5;
    private static final int TIMELINE_VOUCHER_TYPE = 0;
    private static final int TIMELINE_FRIEND_TYPE = 1;
    private static final int TIMELINE_MONTH_TYPE = 2;
    private static final int TIMELINE_USED_VOUCHER_TYPE = 3;
    private static final int TIMELINE_EXPIRING_VOUCHER_TYPE = 4;

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
        new AsyncTask<Object, Void, Boolean>() {
            protected Boolean doInBackground(Object... param) {
                try {
                    reservations.addAll(Net.getReservationsList(token));
                    return true;
                } catch (Exception e) {
                    Log.v("", "Error loading reservations", e);
                    return false;
                }
            }

            protected void onPostExecute(Boolean success) {
                adapter.notifyDataSetChanged();
                if (!success) {
                    MP.track(mixpanel, "Restaurant List", "Failed to load venues");
                    //this means the list is empty!  If you'd like to display
                    //any sort of indicator, here would be the place to do it.
                }
                if (reservations.size() == 0) {
                    (getActivity().findViewById(R.id.noPurchases)).setVisibility(View.VISIBLE);
                }
                int totalKidsFed = 0;

                for (int i = 0; i < reservations.size(); i++) {
                    items.add(getViewItem(reservations.get(i)));
                    totalKidsFed += reservations.get(i).getKidsFed();
                }
                TextView tvKidsFed = (TextView) getActivity().findViewById(R.id.textViewKidFed);
                tvKidsFed.setText(String.format("%d", totalKidsFed));
                app.setTotalKidsFed(totalKidsFed);
            }
        }.execute();

        //TimelineHelper.fillItems(items);
        adapter = new TimelineAdapter(items, new TimelineAdapter.ItemClickListener() {
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

    }

    private ViewItem getViewItem(Reservation reservation) {
        int itemViewType = reservation.getState();
        switch (itemViewType) {
            case TIMELINE_YOU_AND_FRIENDS_TYPE:
                return new TimelineHeaderViewItem(reservation);
            case TIMELINE_VOUCHER_TYPE:
                return new TimelineVoucherViewItem(reservation);
            case TIMELINE_FRIEND_TYPE:
                return new TimelineFriendViewItem(reservation);
            case TIMELINE_MONTH_TYPE:
                return new TimelineMonthViewItem(reservation);
            case TIMELINE_USED_VOUCHER_TYPE:
                return new TimelineUsedVoucherViewItem(reservation);
            case TIMELINE_EXPIRING_VOUCHER_TYPE:
                return new TimelineExpiringVoucherViewItem(reservation);
            default:
                return null;
        }
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

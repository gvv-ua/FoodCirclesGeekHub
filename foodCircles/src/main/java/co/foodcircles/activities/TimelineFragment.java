package co.foodcircles.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Feed;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.json.Reservation;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

public class TimelineFragment extends ListFragment {
	private ReservationAdapter adapter;
	private FoodCirclesApplication app;
	private MixpanelAPI mixpanel;
	private SimpleFacebook mSimpleFacebook;
	private Feed feed;


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
		View view = inflater.inflate(R.layout.timeline_list, null);
		FontSetter.overrideFonts(getActivity(), view);
		final String token = FoodCirclesUtils.getToken(getActivity());
		app.reservations = new ArrayList<Reservation>();
		app.reservations = new ArrayList<Reservation>();
		new AsyncTask<Object, Void, Boolean>() {
			protected Boolean doInBackground(Object... param) {
				try {
					app.reservations.addAll(Net.getReservationsList(token));
					return true;
				} catch (Exception e) {
					Log.v("", "Error loading reservations", e);
					return false;
				}
			}

			protected void onPostExecute(Boolean success) {
				adapter.notifyDataSetChanged();
				if (!success) {
					MP.track(mixpanel, "Restaurant List","Failed to load venues");
					//this means the list is empty!  If you'd like to display 
					//any sort of indicator, here would be the place to do it.
				}
				if (app.reservations.size() == 0) {
					((TextView) getActivity().findViewById(R.id.noPurchases)).setVisibility(View.VISIBLE);
				}
				int totalKidsFed = 0;

				for (int i=0; i<app.reservations.size(); i++) {
					totalKidsFed += app.reservations.get(i).getKidsFed();	
				}
				try {
					TextView tvKidsFed = (TextView) getActivity().findViewById(R.id.textViewKidFed);
					tvKidsFed.setText(totalKidsFed + "");
					app.setTotalKidsFed(totalKidsFed);
				} catch (Exception e) {}

			}
		}.execute();

		adapter = new ReservationAdapter(app.reservations);
		this.setListAdapter(adapter);
		TextView inviteOrImportFriends = (TextView) view.findViewById(R.id.textViewInviteOrImport);

		if (FoodCirclesUtils.isConnectedToSocialAccounts(TimelineFragment.this.getActivity())) 
		{ inviteOrImportFriends.setText("Invite Friends"); }

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
				feed = new Feed.Builder()
				.setMessage("Try FoodCircles!")
				.setName("Savings with a Conscience!")
				.setCaption("Local restaurants, a $1 dish, and $1 donated to feed a hungry child.")
				.setDescription("#bofo: http://www.joinfoodcircles.org @foodcircles ")
				.setPicture(Net.logo).setLink("http://www.joinfoodcircles.org").build();
				if (mSimpleFacebook.isLogin()){
					mSimpleFacebook.publish(feed, true, onPublishListener);
				} else {
					mSimpleFacebook.login(mOnLoginListener);
				}
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(getActivity());
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
			Intent intent = new Intent(getActivity(), AccountOptionsActivity.class);
			startActivity(intent);
			return true;
	    }
	    return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		app.currentReservation = app.reservations.get(position);
		if (app.currentReservation.getState() == 0 || app.currentReservation.getState() == 4) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			ReceiptDialogFragment receiptDialog = new ReceiptDialogFragment();
			receiptDialog.show(fm, "receipt_dialog");
			MP.track(mixpanel, "Timeline", "Opened voucher");
		}
	}

	private class ReservationAdapter extends BaseAdapter {
		public final int YOU_AND_FRIENDS_TYPE = 5;
		public final int VOUCHER_TYPE = 0;
		public final int FRIEND_TYPE = 1;
		public final int MONTH_TYPE = 2;
		public final int USED_VOUCHER_TYPE = 3;
		public final int EXPIRING_VOUCHER_TYPE = 4;
		List<Reservation> reservations;

		@SuppressWarnings("unused")
		private class TimelineHolder {
			public TextView date;
			public TextView venue;
			public TextView childrenFed;
			public TextView me;
			public TextView friends;
			public TextView name;
			public ImageView image;
			public TextView year;
			public TextView month;
			public ImageView settingsButton;
			public TextView kidsFed;
		}

		public ReservationAdapter(List<Reservation> reservations) {
			this.reservations = reservations;
		}

		@SuppressWarnings({ "deprecation", "unused" })
		private boolean compareDatesIfNeedsHr(Date date1, Date date2) {
			if (date1.getMonth() != date2.getMonth() || date1.getYear() != date2.getYear()) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int getCount() {
			return reservations.size();
		}

		@Override
		public int getViewTypeCount() {
			return 6;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return getItemViewType(position) == VOUCHER_TYPE || getItemViewType(position) == EXPIRING_VOUCHER_TYPE;
		}
		
		@Override
		public int getItemViewType(int position) {
			try {
				int itemViewType = reservations.get(position).getState();
				return itemViewType;
			} catch (Exception e) {
				Log.d("","getItemViewType failed!");
				return 1;
			}
		}

		@Override
		public Object getItem(int index) {
			return reservations.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		//This displays the information in each object in the timeline.  
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			TimelineHolder holder = null;
			Reservation reservation = reservations.get(position);
			int itemViewType = reservations.get(position).getState();
			int totalKidsFed = 0;
			switch (itemViewType) {
			case YOU_AND_FRIENDS_TYPE:
					view = getActivity().getLayoutInflater().inflate(R.layout.timeline_top_row, parent, false);
					FontSetter.overrideFonts(parent.getContext(), view);
					holder = new TimelineHolder();
					holder.me = (TextView) view.findViewById(R.id.textViewMe);
					holder.friends = (TextView) view.findViewById(R.id.textViewFriends);
					holder.kidsFed = (TextView) view.findViewById(R.id.textViewKidsFed);
					holder.settingsButton = (ImageView) view.findViewById(R.id.button);
					holder.settingsButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), AccountOptionsActivity.class);
							startActivity(intent);
						}
					});
					if (totalKidsFed == 1) {
						holder.kidsFed.setText(totalKidsFed + " child fed");
					} else {
						holder.kidsFed.setText(totalKidsFed + " children fed");
					}
					view.setTag(holder);
				break;
			case VOUCHER_TYPE:
				view = getActivity().getLayoutInflater().inflate( R.layout.timeline_row, parent, false);
				FontSetter.overrideFonts(parent.getContext(), view);
				holder = new TimelineHolder();
				holder.date = (TextView) view.findViewById(R.id.textViewDate);
				holder.venue = (TextView) view.findViewById(R.id.textViewVenue);
				holder.childrenFed = (TextView) view
						.findViewById(R.id.textViewChildrenFed);
				try {
					holder.date.setText(FoodCirclesUtils.convertLongIntoStringDate(reservation.getDatePurchased()));
				} catch (Exception e) { }
				try {
					holder.venue.setText(reservation.getVenue().getName());
					int kidsFed = reservation.getKidsFed();
					if (kidsFed == 1) {
						holder.childrenFed.setText(kidsFed + " child fed");
					} else {
						holder.childrenFed.setText(kidsFed + " children fed");
					}
				} catch (Exception e) { }
				view.setTag(holder);
				break;
			case EXPIRING_VOUCHER_TYPE:
				view = getActivity().getLayoutInflater().inflate( R.layout.timeline_row_expiring, parent, false);
				FontSetter.overrideFonts(parent.getContext(), view);
				holder = new TimelineHolder();
				holder.date = (TextView) view.findViewById(R.id.textViewDate);
				holder.venue = (TextView) view.findViewById(R.id.textViewVenue);
				holder.childrenFed = (TextView) view.findViewById(R.id.textViewChildrenFed);
				holder.date.setText(FoodCirclesUtils.convertLongIntoStringDate(reservation.getStartsExpiring().getTime()));
				try {
					holder.venue.setText(reservation.getVenue().getName());
					int kidsFed = reservation.getOffer().getChildrenFed();
					if (kidsFed == 1) {
						holder.childrenFed.setText(kidsFed + " child fed");
					} else {
						holder.childrenFed.setText(kidsFed + " children fed");
					}
				} catch (Exception e){ }
				view.setTag(holder);
				break;
			case USED_VOUCHER_TYPE:
				view = getActivity().getLayoutInflater().inflate( R.layout.timeline_row_used, parent, false);
				FontSetter.overrideFonts(parent.getContext(), view);
				holder = new TimelineHolder();
				holder.date = (TextView) view.findViewById(R.id.textViewDate);
				holder.venue = (TextView) view.findViewById(R.id.textViewVenue);
				holder.childrenFed = (TextView) view.findViewById(R.id.textViewChildrenFed);
				holder.date.setText(FoodCirclesUtils.convertLongIntoStringDate(reservation.getDatePurchased()));
				try {
					holder.venue.setText(reservation.getVenue().getName());
					int kidsFed = reservation.getOffer().getChildrenFed();
					if (kidsFed == 1) {
						holder.childrenFed.setText(kidsFed + " child fed");
					} else {
						holder.childrenFed.setText(kidsFed + " children fed");
					}
				} catch (Exception e){}
				view.setTag(holder);
				break;
			case FRIEND_TYPE:
				view = getActivity().getLayoutInflater().inflate(R.layout.timeline_row_friend, parent, false);
				FontSetter.overrideFonts(parent.getContext(), view);
				holder = new TimelineHolder();
				holder.date = (TextView) view.findViewById(R.id.textViewDate);
				holder.venue = (TextView) view.findViewById(R.id.textViewVenue);
				holder.name = (TextView) view.findViewById(R.id.textViewName);
				holder.childrenFed = (TextView) view.findViewById(R.id.textViewChildrenFed);
				holder.image = (ImageView) view.findViewById(R.id.imageView1);
				view.setTag(holder);
				holder.date.setText(reservation.getDatePurchased() + "");
				break;
			case MONTH_TYPE:
				view = getActivity().getLayoutInflater().inflate(R.layout.timeline_row_month, parent, false);
				FontSetter.overrideFonts(parent.getContext(), view);
				holder = new TimelineHolder();
				holder.month = (TextView) view.findViewById(R.id.textViewMonth);
				holder.year = (TextView) view.findViewById(R.id.textViewYear);
				view.setTag(holder);
				String month = FoodCirclesUtils.convertLongToFormattedDateString(reservation.getDatePurchased(), "MMMMM");
				String year = FoodCirclesUtils.convertLongToFormattedDateString(reservation.getDatePurchased(), "yyyy");
				holder.month.setText(month);
				holder.year.setText(year);
				break;
			default:
				break;
			}
			return view;
		}
	}
	
	private OnLoginListener mOnLoginListener = new OnLoginListener() {
		@Override
		public void onFail(String reason) {
			Toast.makeText(getActivity().getBaseContext(), "Facebook Login Failed:" + reason, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onException(Throwable throwable) {
			Toast.makeText(getActivity().getBaseContext(), "Whoops- we've encountered a problem!", Toast.LENGTH_SHORT).show();
			throwable.printStackTrace();
		}

		@Override
		public void onThinking() {
		}

		@Override
		public void onLogin() {
			mSimpleFacebook.publish(feed, true, onPublishListener);
		}

		@Override
		public void onNotAcceptingPermissions(Permission.Type type) {
			Toast.makeText(getActivity().getBaseContext(),"Facebook permissions cancelled!", Toast.LENGTH_SHORT).show();
		}
	};

	private OnPublishListener onPublishListener = new OnPublishListener() {
		@Override
		public void onFail(String reason) {
			Toast.makeText(getActivity().getBaseContext(),"Whoops! The post didn't go through!", Toast.LENGTH_SHORT).show();
		}
		@Override
		public void onException(Throwable throwable) {
			Toast.makeText(getActivity().getBaseContext(),"Whoops! The post didn't go through!", Toast.LENGTH_SHORT).show();
		}
		@Override
		public void onThinking() {
		}

		@Override
		public void onComplete(String postId) {
			Toast.makeText(getActivity().getBaseContext(),"Thanks for sharing the word!", Toast.LENGTH_SHORT).show();
		}
	};
}

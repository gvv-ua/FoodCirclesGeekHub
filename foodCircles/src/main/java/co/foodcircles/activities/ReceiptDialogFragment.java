package co.foodcircles.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.json.Reservation;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

//import com.sromku.simple.fb.Permission;
//import com.sromku.simple.fb.SimpleFacebook;
//import com.sromku.simple.fb.entities.Feed;
//import com.sromku.simple.fb.listeners.OnLoginListener;
//import com.sromku.simple.fb.listeners.OnPublishListener;

public class ReceiptDialogFragment extends DialogFragment {
	public static final String CURRENT_RESERVATION = "CURRENT_RESERVATION";
	private Button markAsUsedButton;
	private FoodCirclesApplication app;
	private TextView textViewVenue;
	private TextView textViewItemName;
	private TextView textViewCode;
	private Reservation reservation;
//	private SimpleFacebook mSimpleFacebook;
//	private Feed feed;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			reservation = getArguments().getParcelable(ReceiptDialogFragment.CURRENT_RESERVATION);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getDialog().getWindow().setBackgroundDrawable(
			new ColorDrawable(android.graphics.Color.TRANSPARENT));
		View v = inflater.inflate(R.layout.voucher_receipt, container, false);
		FontSetter.overrideFonts(getActivity(), v.findViewById(R.id.root));
		app = (FoodCirclesApplication) getActivity().getApplicationContext();
		View teeth = v.findViewById(R.id.viewTiledTeeth);
		textViewCode = (TextView) v.findViewById(R.id.textViewCode);
		textViewItemName = (TextView) v.findViewById(R.id.textViewItemName);
		textViewVenue = (TextView) v.findViewById(R.id.textViewVenue);
		TextView textViewDonated = (TextView) v.findViewById(R.id.textViewDonated);
		TextView textViewChildrenFed = (TextView) v.findViewById(R.id.textViewChildrenFed);
		TextView textViewSecondLine = (TextView) v.findViewById(R.id.textViewMinGroup);
		// Display the purchased voucher
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 1);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = formatter.format(date.getTime());
        String minGroupString = "";
		if ( (reservation != null) && (reservation.getOffer().getMinDiners() > 0)) {
			minGroupString = ("min. group " + reservation.getOffer() .getMinDiners() + ". ");
		}

        textViewSecondLine.setText(minGroupString + "use by "
                + formattedDate);

		if (app.purchasedVoucher) {
			try {
				textViewCode.setText(app.newVoucher.getCode());
			} catch (Exception e) {
				textViewCode.setText(R.string.check_timeline_for_code);
			}
			try {
				Log.d("ReceiptDialogFramgnet","The first one: the textViewItemName is set to " + app.purchasedOffer);
				textViewItemName.setText(app.purchasedOffer);
			} catch (Exception e) {
			}
			try {
				textViewVenue.setText(app.selectedVenue.getName());
			} catch (Exception e) {
				Log.d("", "The venue name could not be recieved!");
			}
			try {
				textViewVenue.setText(app.selectedVenue.getName());
			} catch (Exception e) {}
			textViewDonated.setText("$" + app.purchasedCost + " donated");
			if (app.purchasedCost == 1) {
				textViewChildrenFed.setText("(" + app.purchasedCost
						+ " kids fed)");
			} else {
				textViewChildrenFed.setText("(" + app.purchasedCost + " kids fed)");
			}
		} else {
			try {
				try {
					textViewCode.setText(reservation.getCode());
				} catch (Exception e) { }
				try {
					textViewItemName.setText(reservation.getOffer().getName());
				} catch (Exception e) {}
				try {
					textViewVenue.setText(reservation.getVenue().getName());
				} catch (Exception e) {}
				textViewDonated.setText("$"
						+ reservation.getKidsFed() + " donated");
				if (reservation.getKidsFed() == 1) {
					textViewChildrenFed.setText("("
							+ reservation.getKidsFed()
							+ " kids fed)");
				} else {
					textViewChildrenFed.setText("("
							+ reservation.getKidsFed()
							+ " kids fed)");
				}
				textViewChildrenFed.setText("("
						+ reservation.getKidsFed() + " kids fed)");
			} catch (Exception e) { }
		}

		BitmapDrawable teethDrawable = new BitmapDrawable(getResources(),
				BitmapFactory.decodeResource(getResources(),
						R.drawable.receipt_tooth));
		teethDrawable.setTileModeX(TileMode.REPEAT);

		teeth.setBackground(teethDrawable);

		textViewVenue.setPaintFlags(textViewVenue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		textViewVenue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						VenueProfileActivity.class);
                intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, reservation.getVenue());
				startActivity(intent);
			}
		});

		markAsUsedButton = (Button) v.findViewById(R.id.buttonMarkAsUsed);
		markAsUsedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setMessage("Are you sure?  This can only be done once!")
						.setTitle("Confirm Using Certificate");
				builder.setPositiveButton("OK",
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (reservation == null) {
									Net.markReservationAsUsed(textViewCode.getText().toString().trim());
									app.needsRestart = true;
									ReceiptDialogFragment.this.getActivity()
											.finish();
									startActivity(ReceiptDialogFragment.this
											.getActivity().getIntent());
								} else {
									Net.markReservationAsUsed(reservation
											.getCode().trim());
									app.needsRestart = true;
									ReceiptDialogFragment.this.getActivity()
											.finish();
									startActivity(ReceiptDialogFragment.this
											.getActivity().getIntent());

								}
							}
						});
				builder.setNegativeButton("Cancel", null);
				builder.create().show();
			}
		});

		ImageView facebook = (ImageView) v.findViewById(R.id.imageViewFacebook);
		facebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				String child="child";
//				if(app.currentReservation.getKidsFed()>1)
//					child="children";
//				String shareText=" I've fed "+app.currentReservation.getKidsFed()+" hungry "+child+" simply by eating at "+app.currentReservation.getVenue().getName();
//				feed = new Feed.Builder()
//				.setMessage("Try FoodCircles!")
//				.setName("Savings with a Conscience!")
//				.setCaption(shareText)
//				.setDescription("#bofo: http://www.joinfoodcircles.org� @foodcircles ")
//				.setPicture(Net.logo).setLink("http://www.joinfoodcircles.org").build();
//				if (mSimpleFacebook.isLogin()){
//					mSimpleFacebook.publish(feed, true, onPublishListener);
//				} else {
//					mSimpleFacebook.login(mOnLoginListener);
//				}
			}
		});
		ImageView twitter = (ImageView) v.findViewById(R.id.imageViewTwitter);
		twitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String child="child";
				if(reservation.getKidsFed()>1)
					child="children";
				String shareText="I've fed "+reservation.getKidsFed()+" hungry "+child+" simply by eating at "+reservation.getVenue().getName()+" #bofo. http://www.joinfoodcircles.org";

				Intent shareIntent = findTwitterClient(); 
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share"));
			}
		});
		app.purchasedVoucher = false;
		
		return v;
	}
	
	public Intent findTwitterClient() {
	    final String[] twitterApps = {
	            "com.twitter.android", 
	            "com.twidroid", 
	            "com.handmark.tweetcaster", 
	            "com.thedeck.android" }; 
	    Intent tweetIntent = new Intent();
	    tweetIntent.setType("text/plain");
	    final PackageManager packageManager = getActivity().getPackageManager();
	    List<ResolveInfo> list = packageManager.queryIntentActivities(
	            tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

	    for (int i = 0, ii = twitterApps.length; i < ii; i++) {
	        for (ResolveInfo resolveInfo : list) {
	            String p = resolveInfo.activityInfo.packageName;
	            if (p != null && p.startsWith(twitterApps[i])) {
	                tweetIntent.setPackage(p);
	                return tweetIntent;
	            }
	        }
	    }
	    return tweetIntent;
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
}

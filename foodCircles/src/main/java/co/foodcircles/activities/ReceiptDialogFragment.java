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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.data.VenueList;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Reservation;
import co.foodcircles.json.Venue;
import co.foodcircles.net.Net;
import co.foodcircles.util.AndroidUtils;
import co.foodcircles.util.FacebookShare;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;

public class ReceiptDialogFragment extends DialogFragment {
    private static final String TAG = "ReceiptDialogFragment";
    public static final String CURRENT_RESERVATION = "CURRENT_RESERVATION";
    private FoodCirclesApplication app;
    private TextView textViewCode;
    private Reservation reservation;
    private TextView textViewItemName;
    private TextView textViewVenue;
    private TextView textViewDonated;
    private TextView textViewChildrenFed;
    private TextView textViewSecondLine;
    private FacebookShare facebookShare;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reservation = getArguments().getParcelable(ReceiptDialogFragment.CURRENT_RESERVATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return inflater.inflate(R.layout.voucher_receipt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        facebookShare = (FacebookShare) getActivity();
        FontSetter.overrideFonts(getActivity(), view.findViewById(R.id.root));
        app = (FoodCirclesApplication) getActivity().getApplicationContext();
        View teeth = view.findViewById(R.id.viewTiledTeeth);
        textViewCode = (TextView) view.findViewById(R.id.textViewCode);
        textViewItemName = (TextView) view.findViewById(R.id.textViewItemName);
        textViewVenue = (TextView) view.findViewById(R.id.textViewVenue);
        textViewDonated = (TextView) view.findViewById(R.id.textViewDonated);
        textViewChildrenFed = (TextView) view.findViewById(R.id.textViewChildrenFed);
        textViewSecondLine = (TextView) view.findViewById(R.id.textViewMinGroup);


        if (app.purchasedVoucher) {
            showNewVoucher();
        } else {
            showReservation();
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
                Venue venue = VenueList.getInstance().getById(reservation.getVenue().getId());
                if (venue == null) {
                    venue = reservation.getVenue();
                }
                intent.putExtra(RestaurantActivity.SELECTED_VENUE_KEY, venue);
                startActivity(intent);
            }
        });

        Button markAsUsedButton = (Button) view.findViewById(R.id.buttonMarkAsUsed);
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
                                    markAsUsed(textViewCode.getText().toString().trim());
                                } else {
                                    markAsUsed(reservation.getCode().trim());
                                }
                            }
                        });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });

        ImageView facebook = (ImageView) view.findViewById(R.id.imageViewFacebook);
        facebook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (reservation != null) {
                    String shareText = (reservation.getKidsFed() > 1)
                            ? String.format(getString(R.string.reservation_feed_msg), reservation.getKidsFed(), "children", reservation.getVenue().getName())
                            : String.format(getString(R.string.reservation_feed_msg), reservation.getKidsFed(), "child", reservation.getVenue().getName());
                    facebookShare.shareOnFacebook("Savings with a Conscience! " + shareText + "#bofo: http://www.joinfoodcircles.org� @foodcircles ");
                }
            }
        });
        ImageView twitter = (ImageView) view.findViewById(R.id.imageViewTwitter);
        twitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reservation != null) {
                    String shareText = (reservation.getKidsFed() > 1)
                            ? String.format(getString(R.string.reservation_feed_msg), reservation.getKidsFed(), "children", reservation.getVenue().getName())
                            : String.format(getString(R.string.reservation_feed_msg), reservation.getKidsFed(), "child", reservation.getVenue().getName());
                    TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                            .text(shareText + "#bofo: http://www.joinfoodcircles.org� @foodcircles ")
                            .image(Uri.parse(Net.logo));
                    builder.show();
                }
            }
        });
        app.purchasedVoucher = false;
    }

    private void showReservation() {
        if (reservation != null) {
            textViewCode.setText(reservation.getCode());
            Offer offer = reservation.getOffer();
            if (offer != null) {
                textViewItemName.setText(offer.getName());
                String minGroupString = "";
                if (offer.getMinDiners() > 0) {
                    minGroupString = (String.format(getString(R.string.min_group_diners), offer.getMinDiners()));
                }
                Calendar date = Calendar.getInstance();
                date.add(Calendar.MONTH, 1);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                String formattedDate = formatter.format(date.getTime());
                textViewSecondLine.setText(minGroupString + "use by " + formattedDate);
            }
            Venue venue = reservation.getVenue();
            if (venue != null) {
                textViewVenue.setText(venue.getName());
            }

            textViewDonated.setText(String.format(getString(R.string.usd_donated), reservation.getKidsFed()));
            textViewChildrenFed.setText(String.format(getResources().getQuantityString(R.plurals.children_fed, reservation.getKidsFed()), reservation.getKidsFed()));
        }
    }

    private void showNewVoucher() {
        if (app.newVoucher != null) {
            textViewCode.setText(app.newVoucher.getCode());
            textViewVenue.setText(app.newVoucher.getVenue());
        } else {
            textViewCode.setText(R.string.check_timeline_for_code);
            textViewVenue.setText(R.string.cannot_received_venue_name);
        }
        textViewItemName.setText(app.purchasedOffer);
        textViewDonated.setText(String.format(getString(R.string.usd_donated), app.purchasedCost));


        textViewChildrenFed.setText(String.format(getResources().getQuantityString(R.plurals.children_fed, app.purchasedCost), app.purchasedCost));
    }

    private void markAsUsed(final String code) {
        AndroidUtils.showProgress(getActivity());
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... param) {
                Net.markReservationAsUsed(code);
                return null;
            }

            protected void onPostExecute(Void result) {
                AndroidUtils.dismissProgress();
                app.needsRestart = true;
                ReceiptDialogFragment.this.getActivity()
                        .finish();
                startActivity(ReceiptDialogFragment.this
                        .getActivity().getIntent());
            }
        }.execute();
    }

    private Intent findTwitterClient() {
        final String[] twitterApps = {
                "com.twitter.android",
                "com.twidroid",
                "com.handmark.tweetcaster",
                "com.thedeck.android"};
        Intent tweetIntent = new Intent();
        tweetIntent.setType("text/plain");
        final PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (String twitterApp : twitterApps) {
            for (ResolveInfo resolveInfo : list) {
                String p = resolveInfo.activityInfo.packageName;
                if (p != null && p.startsWith(twitterApp)) {
                    tweetIntent.setPackage(p);
                    return tweetIntent;
                }
            }
        }
        return tweetIntent;
    }
}

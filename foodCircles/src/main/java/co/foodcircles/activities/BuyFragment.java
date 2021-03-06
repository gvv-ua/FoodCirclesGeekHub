package co.foodcircles.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import co.foodcircles.R;
import co.foodcircles.data.CharityList;
import co.foodcircles.exception.NetException2;
import co.foodcircles.json.Charity;
import co.foodcircles.json.Offer;
import co.foodcircles.json.Venue;
import co.foodcircles.json.Voucher;
import co.foodcircles.net.Net;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesApplication;
import co.foodcircles.util.FoodCirclesUtils;

public class BuyFragment extends Fragment {
    private static final String TAG = "BuyFragment";
    private final static int CENTS_IN_DOLLAR = 100;

    private FoodCirclesApplication app;
    private Venue venue;
    private Offer selectedOffer;
    private Charity selectedCharity;
    private Spinner offerSpinner;
    private SeekBar seekBar;
    private TextView price;
    private TextView meals;
    private TextView minPrice, medianPrice, maxPrice;
    private int priceValue;
    private int minPriceValue, medianPriceValue, maxPriceValue;
    private boolean selectedDifferentOffer = false;
    private boolean adjustedSlider = false;
    private boolean selectedDifferentCharity = false;
    private MixpanelAPI mixpanel;

    private static final int REQUEST_CODE_PAYMENT = 1;

    @Override
    public void onStart() {
        super.onStart();
        mixpanel = MixpanelAPI.getInstance(getActivity(), getResources().getString(R.string.mixpanel_token));
    }

    @Override
    public void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            venue = getArguments().getParcelable(RestaurantActivity.SELECTED_VENUE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_options, null);
        FontSetter.overrideFonts(getActivity(), view);

        app = (FoodCirclesApplication) getActivity().getApplicationContext();
        Log.i("Token", FoodCirclesUtils.getToken(getActivity()));
        Log.i("Email", FoodCirclesUtils.getEmail(getActivity()));


        if (FoodCirclesUtils.getToken(getActivity()).isEmpty()
                || FoodCirclesUtils.getEmail(getActivity()).isEmpty()
                || FoodCirclesUtils.getToken(getActivity()).equals("")) {
            Log.i("FALSE", "FALSE");
            try {
                FoodCirclesUtils.saveToken(getActivity(), Net.facebookSignUp(FoodCirclesUtils.getFBUserId(getActivity()), FoodCirclesUtils.getEmail(getActivity())));
            } catch (NetException2 e) {
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("It seems we've run across a problem!  Sorry about that!  Please contact our support team at joinfoodcircles.org/ and we'll work to get it sorted out as soon as possible!")
                        .setTitle("An Error Has Occurred").setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        final List<Offer> offers = venue.getOffers();
        selectedOffer = offers.get(0);
        selectedCharity = CharityList.getInstance().getCharities().get(0);

        offerSpinner = (Spinner) view.findViewById(R.id.spinnerNumFriends);
        @SuppressWarnings("serial")
        ArrayList<String> offersList = new ArrayList<String>() {
            {
                for (Offer offer : offers) {
                    add(offer.getTitle());
                }
            }
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, offersList) {
            public @NonNull
            View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                String str = tv.getText().toString();

                if (str.contains("(")) {

                    Spannable spannable = new SpannableString(str);
                    float textSize = tv.getTextSize();
                    spannable.setSpan(new TextAppearanceSpan(getActivity(), R.style.TextAppearanceNormal), str.indexOf("("), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.secondary_text)), str.indexOf("("), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new AbsoluteSizeSpan((int) textSize), str.indexOf("("), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv.setText(spannable);
                }

                return v;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_content_text);
        offerSpinner.setAdapter(adapter);
        offerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedOffer = offers.get(position);
                selectedDifferentOffer = true;
                minPriceValue = position + 1;
                maxPriceValue = (((minPriceValue) * 10));
                seekBar.setMax(maxPriceValue - (position + 1));
                int range = maxPriceValue - (position + 1);
                medianPriceValue = (range / 2) + position + 1;
                minPrice.setText(NumberFormat.getCurrencyInstance().format(minPriceValue).replaceAll("\\.00", ""));
                medianPrice.setText(NumberFormat.getCurrencyInstance().format(medianPriceValue).replaceAll("\\.00", ""));
                maxPrice.setText(NumberFormat.getCurrencyInstance().format(maxPriceValue).replaceAll("\\.00", ""));
                setPrices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        Spinner donateTo = (Spinner) view.findViewById(R.id.spinnerDonateTo);
        @SuppressWarnings("serial")
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), R.layout.spinner_text, new ArrayList<String>() {
            {
                for (Charity charity : CharityList.getInstance().getCharities())
                    add(charity.getName());
            }
        });
        adapter2.setDropDownViewResource(R.layout.spinner_content_text);
        donateTo.setAdapter(adapter2);
        donateTo.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedDifferentCharity = true;
                selectedCharity = CharityList.getInstance().getCharities().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        price = (TextView) view.findViewById(R.id.textViewTotalPrice);
        meals = (TextView) view.findViewById(R.id.textViewDonated);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(9);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    BuyFragment.this.adjustedSlider = true;
                setPrices();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        minPrice = (TextView) view.findViewById(R.id.textViewPrice1);
        medianPrice = (TextView) view.findViewById(R.id.textViewPrice2);
        maxPrice = (TextView) view.findViewById(R.id.textViewPrice3);
        setPrices();
        Button buyButton = (Button) view.findViewById(R.id.buttonBuy);
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDifferentOffer) MP.track(mixpanel, "Selected Different Offer");
                if (selectedDifferentCharity) MP.track(mixpanel, "Selected Different Charity");
                if (adjustedSlider) MP.track(mixpanel, "Adjusted Slider");
                if (priceValue == minPriceValue)
                    MP.track(mixpanel, "Buying Voucher", "Price", "Minimum Price");
                else if (priceValue < medianPriceValue)
                    MP.track(mixpanel, "Buying Voucher", "Price", "Between Minimum and Regular Price");
                else if (priceValue == medianPriceValue)
                    MP.track(mixpanel, "Buying Voucher", "Price", "Regular Price");
                else if (priceValue < maxPriceValue)
                    MP.track(mixpanel, "Buying Voucher", "Price", "Between Regular and Double Price");
                else MP.track(mixpanel, "Buying Voucher", "Price", "Double Price");


                String paypalOffer = selectedOffer.getTitle() + " from " + venue.getName();
                if (paypalOffer.length() > 22) {
                    paypalOffer = paypalOffer.substring(0, 22);
                }
                paypalOffer = (paypalOffer + "...");
                app.purchasedOffer = selectedOffer.getTitle();
                app.purchasedCost = priceValue;
                app.purchasedGroupSize = selectedOffer.getMinDiners();
                PayPalPayment voucherPayment = new PayPalPayment(new BigDecimal(priceValue), "USD", paypalOffer, PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, BuyOptionsActivity.getConfig());
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, voucherPayment);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        });

        minPrice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(0);
                setPrices();
            }
        });

        medianPrice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getMax() / 2);
                setPrices();
            }
        });

        maxPrice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getMax());
                setPrices();
            }
        });

        ImageView i1 = (ImageView) view.findViewById(R.id.imageViewI1);
        i1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                SimpleDialogFragment dialog = new SimpleDialogFragment();
                dialog.setText("Bringing Friends Description", "Looking to snag an upgrade?  Don't forget to bring along a group of friends!  Or, if you're feeling extra generous, why not offer it to that couple the next table over?");
                dialog.show(fm, "simple_dialog");
            }
        });

        ImageView i2 = (ImageView) view.findViewById(R.id.imageViewI2);
        i2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                SimpleDialogFragment dialog = new SimpleDialogFragment();
                dialog.setText(selectedCharity.getName(), selectedCharity.getDescription());
                dialog.show(fm, "simple_dialog");
            }
        });

        return view;
    }


    //Verify the PayPal sale and add the certificate to the user's account
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            MP.track(mixpanel, "Successful Payment");
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                JSONObject jsonRoot = confirm.toJSONObject();
                if (jsonRoot == null) return;
                JSONObject jsonResponse = jsonRoot.optJSONObject("response");
                if (jsonResponse == null) return;
                final String id = jsonResponse.optString("id", "");
                final String authToken = FoodCirclesUtils.getToken(getActivity());

                if (authToken != null && priceValue != 0 && selectedOffer != null && id != null && selectedCharity != null) {
                    new AsyncTask<Object, Void, Voucher>() {
                        @Override
                        protected Voucher doInBackground(Object... params) {
                            try {
                                return Net.verifyPayment(authToken, priceValue, selectedOffer.getId(), id, selectedCharity.getId());
                            } catch (NetException2 e1) {
                                Log.d(TAG, "NetException exception", e1);
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Voucher voucher) {
                            app.newVoucher = voucher;
                        }
                    }.execute();
                } else {
                    return;
                }

                try {
                    Log.i(TAG, confirm.toJSONObject().toString(4));
                    app.purchasedVoucher = true;
                    app.needsRestart = true;

                    getActivity().finish();
                } catch (JSONException e) {
                    Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        }
    }

    private void setPrices() {
        int voucherLevel = offerSpinner.getSelectedItemPosition();
        int priceAmount = ((seekBar.getProgress() + voucherLevel) * 100);
        priceAmount += CENTS_IN_DOLLAR;
        priceAmount = priceAmount / CENTS_IN_DOLLAR;
        priceValue = priceAmount;
        String priceText = NumberFormat.getCurrencyInstance().format(priceAmount);
        priceText = priceText.replaceAll("\\.00", "");
        price.setText(priceText);

        String msg;
        if (priceAmount > 1) {
            msg = priceAmount + " people";
        } else {
            msg = priceAmount + " person";
        }
        meals.setText(msg);
    }
}

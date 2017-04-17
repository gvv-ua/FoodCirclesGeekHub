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
import co.foodcircles.util.FacebookShare;
import co.foodcircles.util.FontSetter;
import co.foodcircles.util.FoodCirclesUtils;

public class TimelineFragment extends Fragment implements ReservationList.OnDataUpdateSuccessCallback, ReservationList.OnDataUpdateFailCallback{
    private TimelineAdapter adapter;
    private MixpanelAPI mixpanel;
    private FacebookShare facebookShare;

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
        return inflater.inflate(R.layout.timeline_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FontSetter.overrideFonts(getActivity(), view);
        facebookShare = (FacebookShare) getActivity();
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
                facebookShare.shareOnFacebook("Savings with a Conscience! Local restaurants, a $1 dish, and $1 donated to feed a hungry child. #bofo: http://www.joinfoodcircles.org @foodcircles");
            }
        });

        ReservationList.getInstance().updateData(token, this, this);
    }

    @Override
    public void onUpdateVenuesSuccess() {
        if (getActivity() != null) {
            TextView tvKidsFed = (TextView) getActivity().findViewById(R.id.textViewKidFed);
            tvKidsFed.setText(String.format("%d", ReservationList.getInstance().getTotalKidsFed()));

            if (ReservationList.getInstance().getReservations().size() == 0) {
                (getActivity().findViewById(R.id.noPurchases)).setVisibility(View.VISIBLE);
            }
        }
        adapter.updateAdapter(ReservationList.getInstance().getReservations());
    }

    @Override
    public void onUpdateVenuesFailed() {
        MP.track(mixpanel, "Restaurant List", "Failed to load venues");
    }

}

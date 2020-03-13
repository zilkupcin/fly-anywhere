package com.example.android.flyanywhere.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.adapters.DealAdapter;
import com.example.android.flyanywhere.models.Deal;
import com.example.android.flyanywhere.utils.DealUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;


public class DealFragment extends Fragment implements DealAdapter.OnListItemClickListener {

    @BindView(R.id.rv_deal_list)
    RecyclerView dealRecyclerView;

    private static final int FRAGMENT_RELEVANT_DEALS = 1;
    private static final int FRAGMENT_SAVED_DEALS = 0;
    private static final int FRAGMENT_ALL_DEALS = 2;

    private static final String BUNDLE_KEY_FRAGMENT_TYPE = "fragmentType";
    private static final String BUNDLE_KEY_DEAL_ID = "dealId";
    private static final String KEY_RECYCLER_STATE = "recyclerState";

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser currentUser;

    private DatabaseReference mTagReference;
    private DatabaseReference mSavedDealsReference;
    private DatabaseReference mAllDealsReference;

    private ValueEventListener mTagEventListener;
    private ValueEventListener mSavedDealsEventListener;
    private ValueEventListener mAllDealsEventListener;

    private ArrayList<Deal> dealList = new ArrayList<>();
    private DealAdapter mDealAdapter;
    private Bundle mSavedInstanceState;
    private int fragmentType;

    public DealFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deals, container, false);
        ButterKnife.bind(this, view);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(container.getContext());
        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            fragmentType = argsBundle.getInt(BUNDLE_KEY_FRAGMENT_TYPE);
        }
        mDealAdapter = new DealAdapter(fragmentType, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                container.getContext(),
                LinearLayoutManager.VERTICAL,
                false);
        dealRecyclerView.setHasFixedSize(false);
        dealRecyclerView.setLayoutManager(linearLayoutManager);
        dealRecyclerView.setAdapter(mDealAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onListItemClicked(int position) {
        logViewedDeal(position);
        Bundle dealInfo = new Bundle();
        dealInfo.putString(BUNDLE_KEY_DEAL_ID, dealList.get(position).getDealId());
        Intent intent = new Intent(getContext(), DealDetailsActivity.class);
        intent.putExtras(dealInfo);
        startActivity(intent);
    }

    public void getAllDeals() {
        dealList.clear();
        mAllDealsReference = mFirebaseDatabase.getReference()
                .child("deals");
        mAllDealsEventListener = mAllDealsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dealList = DealUtils.getDealsFromSnapshot(dataSnapshot);
                if (fragmentType == FRAGMENT_ALL_DEALS) {
                    mDealAdapter.setDealList(dealList);
                    restoreRecyclerViewState();

                } else if (fragmentType == FRAGMENT_RELEVANT_DEALS) {
                    getUserTags();
                } else if (fragmentType == FRAGMENT_SAVED_DEALS) {
                    getUserSavedDeals();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void restoreRecyclerViewState() {
        if (mSavedInstanceState != null) {
            dealRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedInstanceState
                    .getParcelable(KEY_RECYCLER_STATE));
        }
    }

    public void getUserSavedDeals() {
        mSavedDealsReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/savedDeals");

        mSavedDealsEventListener
                = mSavedDealsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //User has saved deals
                    ArrayList<String> savedDeals = new ArrayList<>();
                    for (DataSnapshot sDeals : dataSnapshot.getChildren()) {
                        if (sDeals.getValue() != null) {
                            savedDeals.add(sDeals.getValue().toString());
                        }
                    }
                    dealList = DealUtils.filterDealsBySaved(savedDeals, dealList);

                } else {
                    // User hasn't saved any deals. Deal list should be empty
                    dealList.clear();
                }

                mDealAdapter.setDealList(dealList);
                restoreRecyclerViewState();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUserTags() {
        mTagReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/subscribedTags/");

        mTagEventListener = mTagReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //User has selected tags
                    ArrayList<String> selectedTags = new ArrayList<>();
                    for (DataSnapshot subscribedTags : dataSnapshot.getChildren()) {
                        if (subscribedTags.getValue() != null) {
                            selectedTags.add(subscribedTags.getValue().toString());
                        }
                    }
                    dealList = DealUtils.filterDealsByRelevancy(selectedTags, dealList);
                } else {
                    // User hasn't selected any tags, list should be empty
                    dealList.clear();
                }

                mDealAdapter.setDealList(dealList);
                restoreRecyclerViewState();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void logViewedDeal(int position) {
        Bundle params = new Bundle();
        params.putString(
                FirebaseAnalytics.Param.ITEM_ID,
                dealList.get(position).getDealId());
        params.putString(
                FirebaseAnalytics.Param.ITEM_NAME,
                dealList.get(position).getTitle());
        params.putString(
                FirebaseAnalytics.Param.ITEM_CATEGORY,
                dealList.get(position).getDealType().toString());

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
    }

    @Override
    public void onResume() {
        setCurrentUser();
        if (currentUser != null) {
            getAllDeals();
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (dealRecyclerView != null) {
            outState.putParcelable(
                    KEY_RECYCLER_STATE,
                    dealRecyclerView.getLayoutManager().onSaveInstanceState());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fragmentType == FRAGMENT_ALL_DEALS) {
            mAllDealsReference.removeEventListener(mAllDealsEventListener);
        } else if (fragmentType == FRAGMENT_RELEVANT_DEALS) {
            mAllDealsReference.removeEventListener(mAllDealsEventListener);
            mTagReference.removeEventListener(mTagEventListener);
        } else if (fragmentType == FRAGMENT_SAVED_DEALS) {
            mAllDealsReference.removeEventListener(mAllDealsEventListener);
            mSavedDealsReference.removeEventListener(mSavedDealsEventListener);
        }
    }
}

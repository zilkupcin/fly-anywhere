package com.example.android.flyanywhere.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.flyanywhere.models.Deal;
import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.utils.DealUtils;
import com.example.android.flyanywhere.utils.NotificationUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by baroc on 23/05/2018.
 */

public class LatestDealsFirebaseJobService extends JobService {
    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    public boolean onStartJob(JobParameters job) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Context context = LatestDealsFirebaseJobService.this;
            getAllDeals(context, job);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private long getLastSeenTime(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getLong(getString(R.string.pref_key_last_seen_time), 0);
    }

    private void getAllDeals(final Context context, final JobParameters job) {
        final DatabaseReference mDealReference = mFirebaseDatabase.getReference()
                .child("deals");
        mDealReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Deal> dealList = new ArrayList<>();
                dealList = DealUtils.getDealsFromSnapshot(dataSnapshot);
                getUserTags(context, job, dealList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserTags(final Context context,
                             final JobParameters job, final ArrayList<Deal> dealList) {
        final DatabaseReference mTagReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/subscribedTags/");
        mTagReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    ArrayList<Deal> filteredList =
                            DealUtils.filterDealsByRelevancy(selectedTags, dealList);
                    NotificationUtils.notifyAboutLatestDeals(
                            context,
                            DealUtils.getNewDealCount(filteredList, getLastSeenTime(context)));
                    jobFinished(job, false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

package com.example.android.flyanywhere.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.android.flyanywhere.services.LatestDealsFirebaseJobService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by baroc on 23/05/2018.
 */

public class ScheduleUtilities {

    private static boolean sInitialized;
    private static final String LATEST_DEALS_JOB_TAG = "latest_deals_job_tag";

    synchronized public static void scheduleLatestDealCheck(@NonNull final Context context) {
        if (!sInitialized) {
            FirebaseJobDispatcher dispatcher =
                    new FirebaseJobDispatcher(new GooglePlayDriver(context));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(LatestDealsFirebaseJobService.class)
                    .setTag(LATEST_DEALS_JOB_TAG)
                    .setRecurring(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(28800, 32400))
                    .setReplaceCurrent(true)
                    .build();
            dispatcher.mustSchedule(myJob);
            sInitialized = true;
        }

    }
}

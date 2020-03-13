package com.example.android.flyanywhere;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by baroc on 09/05/2018.
 */

public class FlyAnywhere extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAnalytics mFirebaseAnalytics;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }

}

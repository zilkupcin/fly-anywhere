package com.example.android.flyanywhere.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.android.flyanywhere.ui.MainActivity;
import com.example.android.flyanywhere.R;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by baroc on 23/05/2018.
 */


public class NotificationUtils {

    private static final int OPEN_MAIN_ACTIVITY_PENDING_INTENT = 0;
    private static final int LATEST_DEAL_NOTIFICATION_ID = 1;
    private static final String LATEST_DEAL_CHANNEL_ID = "latest_deal_channel";


    private static PendingIntent contentIntent(Context context) {
        Intent openMainActivity = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                OPEN_MAIN_ACTIVITY_PENDING_INTENT,
                openMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void notifyAboutLatestDeals(Context context, Integer newDealCount) {
        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        if (newDealCount > 0) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        LATEST_DEAL_CHANNEL_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, LATEST_DEAL_CHANNEL_ID)
                            .setContentTitle(context.getString(R.string.notification_title))
                            .setContentText(context.getString(R.string.notification_message_part1) +
                                    newDealCount +
                                    context.getString(R.string.notification_message_part2))
                            .setSmallIcon(R.drawable.ic_notification_icon)
                            .setContentIntent(contentIntent(context))
                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                            .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            notificationManager.notify(LATEST_DEAL_NOTIFICATION_ID, notificationBuilder.build());
            mFirebaseAnalytics.logEvent(context.getString(R.string.event_notification_open), null);
        }
    }

}

package com.example.android.flyanywhere;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.example.android.flyanywhere.services.WidgetRemoteViewsService;
import com.example.android.flyanywhere.ui.DealDetailsActivity;

/**
 * Implementation of App Widget functionality.
 */
public class SavedDealsWidget extends AppWidgetProvider {


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        if (action != null) {
            if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                final ComponentName cn = new ComponentName(context, SavedDealsWidget.class);
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.lv_saved_deals);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = updateAppWidget(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId,
                    remoteViews);
        }
    }

    private RemoteViews updateAppWidget(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), R.layout.saved_deals_widget);

        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteViews.setRemoteAdapter(appWidgetId, R.id.lv_saved_deals,
                intent);

        Intent goToDetails = new Intent(context, DealDetailsActivity.class);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(goToDetails)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.lv_saved_deals, clickPendingIntentTemplate);
        remoteViews.setEmptyView(R.id.lv_saved_deals, R.id.tv_empty_view);

        return remoteViews;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}


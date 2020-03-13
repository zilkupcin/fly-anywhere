package com.example.android.flyanywhere.adapters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.SavedDealsWidget;
import com.example.android.flyanywhere.models.Deal;
import com.example.android.flyanywhere.utils.DealUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by baroc on 21/05/2018.
 */

public class WidgetDealAdapter implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<Deal> dealList = new ArrayList<>();
    private ArrayList<Deal> tempDealList = new ArrayList<>();
    private Context context;
    private int appWidgetId;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private static final int DEAL_TYPE_ONE_WAY = 0;
    private static final int DEAL_TYPE_RETURN = 1;
    private static final int DEAL_TYPE_AROUND_THE_WORLD = 2;
    private static final int DEAL_TYPE_MULTI_DESTINATION = 3;

    private static final String DEAL_TYPE_TEXT_ONE_WAY = "ONE WAY";
    private static final String DEAL_TYPE_TEXT_RETURN = "RETURN";
    private static final String DEAL_TYPE_TEXT_AROUND_THE_WORLD = "AROUND THE WORLD";
    private static final String DEAL_TYPE_TEXT_MULTI_DESTINATION = "MULTI DESTINATION";

    private boolean mLoadingInitialized;


    public WidgetDealAdapter(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onCreate() {
    }

    private void getAllDeals() {
        final DatabaseReference mDealReference = mFirebaseDatabase.getReference()
                .child("deals");
        mDealReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tempDealList = DealUtils.getDealsFromSnapshot(dataSnapshot);
                getUserSavedDeals();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateAllWidgets() {
        Intent intent = new Intent(context.getApplicationContext(), SavedDealsWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager widgetManager = AppWidgetManager
                .getInstance(context.getApplicationContext());
        int[] ids = widgetManager.getAppWidgetIds(
                new ComponentName(context.getApplicationContext(), SavedDealsWidget.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.getApplicationContext().sendBroadcast(intent);
    }

    private void getUserSavedDeals() {
        final DatabaseReference mUserSavedDealReference = mFirebaseDatabase.getReference()
                .child("users/" + getCurrentUser().getUid() + "/savedDeals");

        mUserSavedDealReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    dealList = DealUtils.filterDealsBySaved(savedDeals, tempDealList);
                    /* Since calls to Firebase DB are asynchronous and the onDataSetChanged() method should
                     * load data synchronously, by the time the loading is finished, the listview
                      * is already updated. Therefore I need to call the notifyAppWidgetViewDataChanged
                      * method after the data is loaded to update the listview with current data*/
                    updateAllWidgets();
                } else {
                    // User hasn't saved any deals. Deal list should be empty
                    dealList.clear();
                    updateAllWidgets();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDataSetChanged() {
        if (getCurrentUser() != null) {
            if (!mLoadingInitialized) {
                mLoadingInitialized = true;
                getAllDeals();
            } else {
                mLoadingInitialized = false;
            }
        } else {
            dealList.clear();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (dealList != null) {
            return dealList.size();
        } else {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(final int i) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_deal_item);
        if (i < dealList.size()) {
            remoteView.setTextViewText(R.id.tv_deal_title, dealList.get(i).getTitle());
            remoteView.setTextViewText(R.id.tv_departure_cities, dealList.get(i).getDepartureCity());
            remoteView.setTextViewText(R.id.tv_arrival_cities, dealList.get(i).getDestination());
            remoteView.setTextViewText(R.id.tv_one_way_dates, dealList.get(i).getDepartureDates());
            remoteView.setTextViewText(R.id.tv_return_dates, dealList.get(i).getReturnDates());

            if (dealList.get(i).getDealType() == DEAL_TYPE_ONE_WAY) {
                remoteView.setInt(R.id.iv_return_dates_icon, "setVisibility", View.GONE);
                remoteView.setInt(R.id.tv_return_dates, "setVisibility", View.GONE);
                remoteView.setTextViewText(R.id.tv_deal_type, DEAL_TYPE_TEXT_ONE_WAY);
            } else if (dealList.get(i).getDealType() == DEAL_TYPE_RETURN) {
                remoteView.setTextViewText(R.id.tv_return_dates, dealList.get(i).getReturnDates());
                remoteView.setTextViewText(R.id.tv_deal_type, DEAL_TYPE_TEXT_RETURN);
            } else if (dealList.get(i).getDealType() == DEAL_TYPE_AROUND_THE_WORLD) {
                remoteView.setTextViewText(R.id.tv_deal_type, DEAL_TYPE_TEXT_AROUND_THE_WORLD);
                remoteView.setTextViewText(R.id.tv_return_dates, dealList.get(i).getReturnDates());
            } else if (dealList.get(i).getDealType() == DEAL_TYPE_MULTI_DESTINATION) {
                remoteView.setTextViewText(R.id.tv_deal_type, DEAL_TYPE_TEXT_MULTI_DESTINATION);
            }

            if (dealList.get(i).getExpiredReports().size() >= 5) {
                remoteView.setBoolean(R.id.btn_price_button, "setEnabled", false);
                remoteView.setTextViewText(R.id.btn_price_button, context.getString(R.string.button_text_expired));
            } else {
                remoteView.setBoolean(R.id.btn_price_button, "setEnabled", true);
                remoteView.setTextViewText(R.id.btn_price_button, context.getString(R.string.deal_price_from) + dealList.get(i).getDealPrice());
            }

            try {
                Bitmap bitmap = Picasso.get()
                        .load(dealList.get(i).getImageUrl()).get();
                remoteView.setImageViewBitmap(R.id.iv_deal_header_image, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(context.getString(R.string.deal_key_deal_id), dealList.get(i).getDealId());
            remoteView.setOnClickFillInIntent(R.id.btn_price_button, fillInIntent);
        }

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}

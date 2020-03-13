package com.example.android.flyanywhere.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.SavedDealsWidget;
import com.example.android.flyanywhere.models.Deal;
import com.example.android.flyanywhere.utils.DealUtils;
import com.example.android.flyanywhere.utils.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DealDetailsActivity extends AppCompatActivity {

    private String dealId;
    private String searchLink;
    private String departureCity;
    private String destination;
    private String departureDates;
    private String returnDates;
    private String title;
    private String imageUrl;
    private String description;
    private String postedBy;
    private ArrayList<String> dealTags = new ArrayList<>();
    private ArrayList<String> expiredReports = new ArrayList<>();
    private Integer dealType;
    private Integer dealPrice;
    private long createdDate;

    private Bundle mSavedInstanceState;
    private Boolean mRestored;

    private ArrayList<String> savedDeals = new ArrayList<>();

    private FirebaseUser currentUser;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int MENU_SAVE = 0;
    private static final int MENU_REMOVE = 1;
    private static final int MENU_REPORT = 2;

    private static final String DATE_FORMAT = "dd-MMM-yyyy";

    private DatabaseReference mSavedDealsReference;
    private DatabaseReference mCurrentDealReference;
    private ValueEventListener mCurrentDealListener;
    private ValueEventListener mSavedDealsListener;

    @BindView(R.id.iv_deal_header_image)
    ImageView dealHeaderImage;
    @BindView(R.id.tv_deal_title)
    TextView dealTitleTextView;
    @BindView(R.id.tv_deal_date_author)
    TextView dealDateAuthorTextView;
    @BindView(R.id.tv_flight_type_tag)
    TextView flightTypeTagTextView;
    @BindView(R.id.tv_departure_cities)
    TextView departureCitiesTextView;
    @BindView(R.id.tv_arrival_cities)
    TextView arrivalCitiesTextView;
    @BindView(R.id.tv_one_way_dates)
    TextView oneWayDatesTextView;
    @BindView(R.id.iv_return_dates_icon)
    ImageView returnDatesIcon;
    @BindView(R.id.tv_return_dates)
    TextView returnDatesTextView;
    @BindView(R.id.tv_deal_description)
    TextView dealDescriptionTextView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.cl_details)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView mScrollView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_details);
        ButterKnife.bind(this);
        getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (savedInstanceState != null) {
            mSavedInstanceState = savedInstanceState;
            restoreDeal();
        } else {
            mRestored = true;
        }
        setUpActionBar();
        getDealIdFromIntent();
        getUserSavedDeals();
    }

    public void searchDeal(View view) {
        Bundle params = new Bundle();
        params.putString(getString(R.string.param_key_dealid), dealId);
        mFirebaseAnalytics.logEvent(getString(R.string.event_key_deal_search), params);
        Intent openSearchLink = new Intent(Intent.ACTION_VIEW);
        openSearchLink.setData(Uri.parse(searchLink));
        startActivity(openSearchLink);
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateAllWidgets() {
        Intent intent = new Intent(getApplicationContext(), SavedDealsWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] ids = widgetManager.getAppWidgetIds(
                new ComponentName(getApplicationContext(), SavedDealsWidget.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getApplicationContext().sendBroadcast(intent);
    }

    private void restoreDeal() {
        departureCity = mSavedInstanceState.getString(getString(R.string.deal_key_departure_city));
        destination = mSavedInstanceState.getString(getString(R.string.deal_key_destination));
        departureDates = mSavedInstanceState.getString(getString(R.string.deal_key_departure_dates));
        returnDates = mSavedInstanceState.getString(getString(R.string.deal_key_return_dates));
        title = mSavedInstanceState.getString(getString(R.string.deal_key_title));
        imageUrl = mSavedInstanceState.getString(getString(R.string.deal_key_image_url));
        description = mSavedInstanceState.getString(getString(R.string.deal_key_description));
        postedBy = mSavedInstanceState.getString(getString(R.string.deal_key_postedby));
        dealTags = mSavedInstanceState.getStringArrayList(getString(R.string.deal_key_deal_tags));
        expiredReports = mSavedInstanceState.getStringArrayList(getString(R.string.deal_key_expired_reports));
        dealType = mSavedInstanceState.getInt(getString(R.string.deal_key_deal_type));
        dealPrice = mSavedInstanceState.getInt(getString(R.string.deal_key_deal_price));
        createdDate = mSavedInstanceState.getLong(getString(R.string.deal_key_created_date));
        dealId = mSavedInstanceState.getString(getString(R.string.deal_key_deal_id));
        searchLink = mSavedInstanceState.getString(getString(R.string.deal_key_deal_link));
        setUpDeal();
        mRestored = true;
    }

    public void getDealIdFromIntent() {
        if (getIntent().getExtras() != null) {
            Bundle dealInfo = getIntent().getExtras();
            dealId = dealInfo.getString(getString(R.string.deal_key_deal_id));
        }
    }

    private void getCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent goToMainActivity =
                    new Intent(DealDetailsActivity.this, MainActivity.class);
            startActivity(goToMainActivity);
            DealDetailsActivity.this.finish();
        }
    }

    public String getDateAndAuthor(long timestamp, String author) {
        Date date = new java.util.Date(timestamp * 1000L);
        SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(date);
        return getString(R.string.posted_by_pt1) + " " +
                author + " " + getString(R.string.posted_by_pt2) + " " + formattedDate;
    }


    private void setUpDeal() {
        dealTitleTextView.setText(title);
        dealDateAuthorTextView.setText(getDateAndAuthor(createdDate, postedBy));
        if (dealType == 0) {
            flightTypeTagTextView.setText(R.string.deal_type_one_way);
            returnDatesTextView.setVisibility(View.GONE);
            returnDatesIcon.setVisibility(View.GONE);
        } else if (dealType == 1) {
            flightTypeTagTextView.setText(R.string.deal_type_return);
            returnDatesTextView.setVisibility(View.VISIBLE);
            returnDatesIcon.setVisibility(View.VISIBLE);
        } else if (dealType == 2) {
            flightTypeTagTextView.setText(R.string.deal_type_around_the_world);
            returnDatesTextView.setVisibility(View.VISIBLE);
            returnDatesIcon.setVisibility(View.VISIBLE);
        } else if (dealType == 3) {
            flightTypeTagTextView.setText(R.string.deal_type_multi_destination);
            returnDatesTextView.setVisibility(View.VISIBLE);
            returnDatesIcon.setVisibility(View.VISIBLE);
        }
        departureCitiesTextView.setText(departureCity);
        arrivalCitiesTextView.setText(destination);
        oneWayDatesTextView.setText(departureDates);
        returnDatesTextView.setText(returnDates);
        dealDescriptionTextView.setText(Html.fromHtml(description));
        Picasso.get().load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(dealHeaderImage);
    }

    public void saveDeal() {
        getCurrentUser();
        savedDeals.add(dealId);
        final DatabaseReference mSavedIdsReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/savedDeals/");
        mSavedIdsReference.setValue(savedDeals, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Snackbar.make(
                            mCoordinatorLayout,
                            R.string.deal_saved_success_message,
                            Snackbar.LENGTH_SHORT).show();
                    updateAllWidgets();
                }
            }
        });
    }

    public void removeDeal() {
        getCurrentUser();
        savedDeals.remove(dealId);
        final DatabaseReference mSavedIdsReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/savedDeals/");
        mSavedIdsReference.setValue(savedDeals, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Snackbar.make(
                            mCoordinatorLayout,
                            R.string.deal_removed_success_message,
                            Snackbar.LENGTH_SHORT).show();
                    updateAllWidgets();
                }
            }
        });
    }

    private void reportDeal() {
        getCurrentUser();
        if (!checkIfExpired()) {
            if (!checkIfAlreadyReported()) {
                expiredReports.add(currentUser.getUid());
                final DatabaseReference mSavedIdsReference = mFirebaseDatabase.getReference()
                        .child("deals/" + dealId + "/expiredReports");
                mSavedIdsReference.setValue(expiredReports, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Snackbar.make(
                                    mCoordinatorLayout,
                                    R.string.deal_reported_message,
                                    Snackbar.LENGTH_SHORT).show();
                            updateAllWidgets();
                        }else {
                            Snackbar.make(
                                    mCoordinatorLayout,
                                    databaseError.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case MENU_SAVE:
                if (NetworkUtils.isConnectedToTheInternet(this)) {
                    saveDeal();
                } else {
                    showRequiresInternetSnackbar();
                }
                break;
            case MENU_REMOVE:
                if (NetworkUtils.isConnectedToTheInternet(this)) {
                    removeDeal();
                } else {
                    showRequiresInternetSnackbar();
                }
                break;
            case MENU_REPORT:
                if (NetworkUtils.isConnectedToTheInternet(this)) {
                    showReportDialog();
                } else {
                    showRequiresInternetSnackbar();
                }
                break;
        }
        return true;
    }

    private void showRequiresInternetSnackbar() {
        Snackbar.make(
                mCoordinatorLayout,
                R.string.error_internet_required_message,
                Snackbar.LENGTH_LONG).show();
    }

    private void showReportDialog() {
        final AlertDialog dialog
                = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog)
                .setTitle(R.string.alert_title)
                .setMessage(R.string.alert_message_report_expired)
                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportDeal();
                    }
                })
                .setNegativeButton(R.string.alert_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private Boolean checkIfSaved(String id) {
        Boolean isSaved = false;
        for (String dealId : savedDeals) {
            if (dealId.equals(id)) {
                isSaved = true;
            }
        }
        return isSaved;
    }

    private Boolean checkIfExpired() {
        if (expiredReports.size() >= 5) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkIfAlreadyReported() {
        Boolean alreadyReported = false;
        for (String id : expiredReports) {
            if (id.equals(currentUser.getUid())) {
                alreadyReported = true;
            }
        }
        return alreadyReported;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (checkIfSaved(dealId)) {
            menu.add(0, MENU_REMOVE, Menu.NONE, getString(R.string.menu_remove_a_deal))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            menu.add(0, MENU_SAVE, Menu.NONE, getString(R.string.menu_save_a_deal))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        if (expiredReports.size() < 5 && !checkIfAlreadyReported()) {
            menu.add(0, MENU_REPORT, Menu.NONE, getString(R.string.menu_report_a_deal))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    public void getUserSavedDeals() {
        mSavedDealsReference = mFirebaseDatabase.getReference()
                .child("users/" + currentUser.getUid() + "/savedDeals/");

        mSavedDealsListener = mSavedDealsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //User has saved deals
                    ArrayList<String> savedDealIds = new ArrayList<>();
                    for (DataSnapshot savedDeal : dataSnapshot.getChildren()) {
                        savedDealIds.add(savedDeal.getValue().toString());
                    }
                    savedDeals.clear();
                    savedDeals.addAll(savedDealIds);
                } else {
                    // User hasn't saved any deals, list should be empty
                    savedDeals.clear();
                }
                getCurrentDeal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentDeal() {
        mCurrentDealReference = mFirebaseDatabase.getReference()
                .child("deals/" + dealId);

        mCurrentDealListener = mCurrentDealReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (mRestored) {
                    Deal currentDeal = DealUtils.getSingleDealFromSnapshot(dataSnapshot);
                    departureCity = currentDeal.getDepartureCity();
                    destination = currentDeal.getDestination();
                    departureDates = currentDeal.getDepartureDates();
                    returnDates = currentDeal.getReturnDates();
                    title = currentDeal.getTitle();
                    imageUrl = currentDeal.getImageUrl();
                    description = currentDeal.getDescription();
                    postedBy = currentDeal.getPostedBy();
                    dealTags = currentDeal.getDealTags();
                    expiredReports = currentDeal.getExpiredReports();
                    dealType = currentDeal.getDealType();
                    dealPrice = currentDeal.getDealPrice();
                    createdDate = currentDeal.getCreatedDate();
                    dealId = currentDeal.getDealId();
                    searchLink = currentDeal.getSearchLink();
                    setUpDeal();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.deal_key_deal_id), dealId);
        outState.putString(getString(R.string.deal_key_departure_city), departureCity);
        outState.putString(getString(R.string.deal_key_destination), destination);
        outState.putString(getString(R.string.deal_key_departure_dates), departureDates);
        outState.putString(getString(R.string.deal_key_return_dates), returnDates);
        outState.putString(getString(R.string.deal_key_title), title);
        outState.putString(getString(R.string.deal_key_image_url), imageUrl);
        outState.putString(getString(R.string.deal_key_description), description);
        outState.putString(getString(R.string.deal_key_postedby), postedBy);
        outState.putString(getString(R.string.deal_key_deal_link), searchLink);
        outState.putInt(getString(R.string.deal_key_deal_type), dealType);
        outState.putInt(getString(R.string.deal_key_deal_price), dealPrice);
        outState.putLong(getString(R.string.deal_key_created_date), createdDate);
        outState.putStringArrayList(getString(R.string.deal_key_deal_tags), dealTags);
        outState.putStringArrayList(getString(R.string.deal_key_expired_reports), expiredReports);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCurrentDealReference.removeEventListener(mCurrentDealListener);
        mSavedDealsReference.removeEventListener(mSavedDealsListener);

    }
}

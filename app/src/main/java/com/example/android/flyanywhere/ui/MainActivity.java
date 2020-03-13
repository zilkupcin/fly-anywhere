package com.example.android.flyanywhere.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.SavedDealsWidget;
import com.example.android.flyanywhere.adapters.ViewPagerAdapter;
import com.example.android.flyanywhere.utils.NetworkUtils;
import com.example.android.flyanywhere.utils.ScheduleUtilities;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.dl_main_root)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_main)
    NavigationView navigationView;
    @BindView(R.id.tl_main)
    PagerSlidingTabStrip mTabLayout;
    @BindView(R.id.vp_main)
    ViewPager mViewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final int RC_SIGN_IN = 123;
    private static final String PREF_KEY_LAST_SEEN_TIME = "last_seen_time";

    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getCurrentUser();
        ViewPagerAdapter mViewPagerAdapter =
                new ViewPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);
        setUpToolbar();
        setLastSeenTime();
        ScheduleUtilities.scheduleLatestDealCheck(this);
        if (!NetworkUtils.isConnectedToTheInternet(getApplicationContext())) {
            Snackbar.make(
                    mDrawerLayout,
                    R.string.error_no_internet_main,
                    Snackbar.LENGTH_LONG).show();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_browse:
                        break;
                    case R.id.nav_credits:
                        Intent creditsIntent =
                                new Intent(MainActivity.this, CreditsActivity.class);
                        startActivity(creditsIntent);
                        break;
                    case R.id.nav_edit_tags:
                        Intent editTagsIntent =
                                new Intent(MainActivity.this, TagActivity.class);
                        startActivity(editTagsIntent);
                        break;
                    case R.id.nav_log_out:
                        signOut();
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

    }

    private void hideUi() {
        mDrawerLayout.setVisibility(View.INVISIBLE);
    }

    private void showUi() {
        mDrawerLayout.setVisibility(View.VISIBLE);
    }

    private void setUpToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
    }

    private void signIn() {
        hideUi();
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateWidgets();
                        signIn();
                    }
                });
    }

    private void updateWidgets() {
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

    private void getCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            signIn();
        }
    }

    private long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    private void setLastSeenTime() {
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(PREF_KEY_LAST_SEEN_TIME, getCurrentTime());
        editor.apply();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigationView.setCheckedItem(R.id.nav_browse);
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                getCurrentUser();
                updateWidgets();
                showUi();
            } else {
                Toast.makeText(this, R.string.error_sign_in_failed, Toast.LENGTH_SHORT).show();
                signIn();
            }
        }
    }

}

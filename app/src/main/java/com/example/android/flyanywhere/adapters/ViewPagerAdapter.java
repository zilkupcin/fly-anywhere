package com.example.android.flyanywhere.adapters;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.flyanywhere.ui.DealFragment;
import com.example.android.flyanywhere.ui.PostDealFragment;
import com.example.android.flyanywhere.R;

/**
 * Created by baroc on 01/05/2018.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    private static final int FRAGMENT_RELEVANT_DEALS = 1;
    private static final int FRAGMENT_ALL_DEALS = 2;
    private static final int FRAGMENT_SAVED_DEALS = 0;
    private static final String BUNDLE_KEY_FRAGMENT_TYPE = "fragmentType";

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle argsBundle = new Bundle();

        if (position == 0) {
            argsBundle.putInt(BUNDLE_KEY_FRAGMENT_TYPE, FRAGMENT_SAVED_DEALS);
            DealFragment fragment = new DealFragment();
            fragment.setArguments(argsBundle);
            return fragment;
        } else if (position == 1) {
            argsBundle.putInt(BUNDLE_KEY_FRAGMENT_TYPE, FRAGMENT_RELEVANT_DEALS);
            DealFragment fragment = new DealFragment();
            fragment.setArguments(argsBundle);
            return fragment;
        } else if (position == 2) {
            argsBundle.putInt(BUNDLE_KEY_FRAGMENT_TYPE, FRAGMENT_ALL_DEALS);
            DealFragment fragment = new DealFragment();
            fragment.setArguments(argsBundle);
            return fragment;
        } else {
            return new PostDealFragment();
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.tab_saved_deals);
            case 1:
                return mContext.getString(R.string.tab_relevant_deals);
            case 2:
                return mContext.getString(R.string.tab_all_deals);
            case 3:
                return mContext.getString(R.string.tab_post_deal);
            default:
                return null;
        }
    }
}

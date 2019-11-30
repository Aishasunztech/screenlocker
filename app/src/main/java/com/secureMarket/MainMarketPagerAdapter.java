package com.secureMarket;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.secure.launcher.R;

import com.secureMarket.ui.home.InstalledAppsFragment;
import com.secureMarket.ui.home.MarketFragment;
import com.secureMarket.ui.home.UpdateAppsFragment;

public class MainMarketPagerAdapter extends FragmentPagerAdapter {

    private MarketFragment marketFragment;
    private InstalledAppsFragment installedAppsFragment;
    private UpdateAppsFragment updateAppsFragment;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    public MainMarketPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return new MarketFragment();
            case 1:
                return new UpdateAppsFragment();
            case 2:
                return new InstalledAppsFragment();

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                marketFragment = (MarketFragment) createdFragment;
                break;
            case 1:
                updateAppsFragment = (UpdateAppsFragment)createdFragment;
                break;
            case 2:
                installedAppsFragment = (InstalledAppsFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

    public MarketFragment getMarketFragment() {
        return marketFragment;
    }

    public InstalledAppsFragment getInstalledAppsFragment() {
        return installedAppsFragment;
    }

    public UpdateAppsFragment getUpdateAppsFragment() {
        return updateAppsFragment;
    }
}
package com.secureMarket;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.screenlocker.secure.R;

public class MainMarketPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
    public MainMarketPagerAdapter(@NonNull FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                MarketFragment fragment = new MarketFragment();
                Bundle b = new Bundle();
                b.putString("check","install");
                fragment.setArguments(b);

                return fragment;

            case 1:
                MarketFragment fragmentUninstall = new MarketFragment();
                Bundle b1 = new Bundle();
                b1.putString("check","uninstall");
                fragmentUninstall.setArguments(b1);

                return fragmentUninstall;
            case 2:
                MarketFragment fragmentUpdate = new MarketFragment();
                Bundle b3 = new Bundle();
                b3.putString("check","update");
                fragmentUpdate.setArguments(b3);

                return fragmentUpdate;
            default:
                MarketFragment fragmentDefault = new MarketFragment();
                Bundle b2 = new Bundle();
                b2.putString("check","install");

                fragmentDefault.setArguments(b2);

                return fragmentDefault;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return context.getResources().getString(R.string.install);
            case 1:
                return context.getResources().getString(R.string.uninstall);
            case 2:
                return context.getResources().getString(R.string.updates);
            default:
                return context.getResources().getString(R.string.install);

        }

    }
}

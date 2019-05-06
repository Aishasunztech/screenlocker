package com.secureMarket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MarketPageAdapter extends FragmentStatePagerAdapter {
    public MarketPageAdapter(@NonNull FragmentManager fm) {
        super(fm);
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
        return 2;
    }
}

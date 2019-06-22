package com.screenlocker.secure.settings.Wallpaper;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.screenlocker.secure.utils.AppConstants.KEY_CODE;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;

public class ChangeWallpaper extends AppCompatActivity
        implements WallpaperAdapter.OnClickListner, SetWallpaperDialog.OnSetWallpaperListener {
    List<Integer> ids = new ArrayList<>();
    private String type;
    private FragmentManager fragmentManager;
    private boolean isBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wallpaper2);
        type = getIntent().getStringExtra("TYPE");
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.wallpapers_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ids.add(R.raw.beatifulhome);
        ids.add(R.raw.bike);
        ids.add(R.raw.building);
        ids.add(R.raw.flower);
        ids.add(R.raw.flower2);
        ids.add(R.raw.parrot1);
        ids.add(R.raw.pattern);
        ids.add(R.raw.sea);
        ids.add(R.raw.bulbul);
        ids.add(R.raw.bus1234);
        ids.add(R.raw.carssas);
        ids.add(R.raw.colorsspalsh);
        ids.add(R.raw.remountan);
        ids.add(R.raw.tower);
        ids.add(R.raw.tower2);
        ids.add(R.raw.wolf);
        ids.add(R.raw.parrot);
        ids.add(R.drawable.default_background);
        ids.add(R.drawable.guest_space);
        ids.add(R.raw.roses);
        ids.add(R.drawable.backgroud_test);
        ids.add(R.raw.royal_enfield);
        ids.add(R.drawable.background_icon);
        ids.add(R.raw.texture);
        ids.add(R.raw.universe);
        ids.add(R.raw.balls);
        ids.add(R.raw.butterfly);
        ids.add(R.raw.citygreen);
        ids.add(R.raw.colorsexposion);
        ids.add(R.raw.greengrass);
        ids.add(R.raw.universe2);
        ids.add(R.raw.secure_scan_screen);
        ids.add(R.raw.audiblack);
        ids.add(R.raw.blackwallpaper);
        ids.add(R.raw.bookandbloon);
        ids.add(R.raw.faster);
        ids.add(R.raw.fhdanimals);
        ids.add(R.raw.groot);
        ids.add(R.raw.gun);
        ids.add(R.raw.pikachu);
        ids.add(R.raw.red_flowrs);
        ids.add(R.raw.stadium);

        WallpaperAdapter adapter = new WallpaperAdapter(ids, this, this);
        RecyclerView rvWallpaper = findViewById(R.id.RVWallpaper);
        rvWallpaper.setLayoutManager(new GridLayoutManager(this, 3));
        rvWallpaper.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position) {
        fragmentManager = getSupportFragmentManager();
        SetWallpaperDialog newFragment = new SetWallpaperDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("RAWID", ids.get(position));
        newFragment.setArguments(bundle);
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(android.R.id.content, newFragment)
                .addToBackStack(null).commit();
    }

    @Override
    public void onWallpaperSelected(int id) {
        fragmentManager.popBackStack();
        switch (type) {
            case KEY_MAIN:
                Toast.makeText(this, getResources().getString(R.string.bg_set_encrypted), Toast.LENGTH_SHORT).show();
                PrefUtils.saveStringPref(ChangeWallpaper.this, AppConstants.KEY_MAIN_IMAGE, String.valueOf(id));
                break;
            case KEY_GUEST:
                Toast.makeText(this, getResources().getString(R.string.bg_set_guest), Toast.LENGTH_SHORT).show();
                PrefUtils.saveStringPref(ChangeWallpaper.this, AppConstants.KEY_GUEST_IMAGE, String.valueOf(id));
                break;
            case KEY_CODE:
                Toast.makeText(this, getResources().getString(R.string.bg_set_lockscreen), Toast.LENGTH_SHORT).show();
                PrefUtils.saveStringPref(ChangeWallpaper.this, AppConstants.KEY_LOCK_IMAGE, String.valueOf(id));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isBackPressed)
        {
            this.finish();
        }
    }
}

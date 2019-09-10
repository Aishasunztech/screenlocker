package com.screenlocker.secure.settings.Wallpaper;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
    private SetWallpaperDialog newFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wallpaper2);
        type = getIntent().getStringExtra("TYPE");
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.wallpapers_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ids.add(R.raw._1231);
        ids.add(R.raw._1232);
        ids.add(R.raw._1234);
        ids.add(R.raw._1235);
        ids.add(R.raw._1236);
        ids.add(R.raw._1237);
        ids.add(R.raw._1238);
        ids.add(R.raw._1239);
        ids.add(R.raw._12310);
        ids.add(R.raw._12311);
        ids.add(R.raw._12312);
        ids.add(R.raw._12314);
        ids.add(R.raw._12315);
        ids.add(R.raw._12316);
        ids.add(R.raw._12317);
        ids.add(R.raw._12318);
        ids.add(R.raw._12319);
        ids.add(R.raw._12320);
        ids.add(R.raw._12321);
        ids.add(R.raw._12323);
        ids.add(R.raw._12);
        ids.add(R.raw._13);
        ids.add(R.raw._14);
        ids.add(R.raw._15);
        ids.add(R.raw._16);
        ids.add(R.raw._17);
        ids.add(R.raw._18);
        ids.add(R.raw._19);
        ids.add(R.raw._20);
        ids.add(R.raw._21);
        ids.add(R.raw._22);
        ids.add(R.raw._23);
        ids.add(R.raw._24);
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
        ids.add(R.raw.red_flowrs);
        ids.add(R.raw.stadium);
        ids.add(R.raw._1232);

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
        if (newFragment == null) {
            newFragment = new SetWallpaperDialog(ChangeWallpaper.this, this);
            Window window = newFragment.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                newFragment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        if (!newFragment.isShowing()) {
            newFragment.setImage(ids.get(position));
            newFragment.show();

        }
    }

    @Override
    public void onWallpaperSelected(int id) {
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
        newFragment.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isBackPressed) {
//            this.finish();
        }
    }
}

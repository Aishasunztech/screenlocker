package com.screenlocker.secure.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;

public class ManagePasswords extends BaseActivity implements View.OnClickListener {
    private ConstraintLayout rootLayout;


    private SettingsActivity settingsActivity;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_passwords);
        setIds();
        setListeners();
        settingsActivity = new SettingsActivity();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * set listeners
     */
    private void setListeners() {
        findViewById(R.id.tvSetGuestPassword).setOnClickListener(this);
        findViewById(R.id.tvSetMainPassword).setOnClickListener(this);
        findViewById(R.id.tvSetDuressPassword).setOnClickListener(this);
        setToolbar(mToolbar);
    }

    private void setIds() {
        rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);
    }

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Passwords");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetGuestPassword:   // handle the set guest password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
                settingsActivity.handleSetGuestPassword(ManagePasswords.this, rootLayout);
                break;

            case R.id.tvSetMainPassword:    // handle the set main password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
                settingsActivity.handleSetMainPassword(ManagePasswords.this, rootLayout);
                break;

            case R.id.tvSetDuressPassword:    // handle the set duress password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */

                settingsActivity.handleSetDuressPassword(ManagePasswords.this, rootLayout);
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

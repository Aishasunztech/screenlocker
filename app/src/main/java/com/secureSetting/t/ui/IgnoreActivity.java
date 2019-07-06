package com.secureSetting.t.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.secureSetting.t.data.IgnoreItem;
import com.secureSetting.t.db.DbIgnoreExecutor;
import com.secureSetting.t.util.AppUtil;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class IgnoreActivity extends BaseActivity {

    private IgnoreAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore);

       setToolbar();

        RecyclerView mList = findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new IgnoreAdapter();
        mList.setAdapter(mAdapter);

        new MyAsyncTask(this).execute();
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Void, Void, List<IgnoreItem>> {

        private WeakReference<Context> mContext;

        MyAsyncTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected List<IgnoreItem> doInBackground(Void... voids) {
            return DbIgnoreExecutor.getInstance().getAllItems();
        }

        @Override
        protected void onPostExecute(List<IgnoreItem> ignoreItems) {
            if (mContext.get() != null && ignoreItems.size() > 0) {
                for (IgnoreItem item : ignoreItems) {
                    item.mName = AppUtil.parsePackageName(mContext.get().getPackageManager(), item.mPackageName);
                }
                mAdapter.setData(ignoreItems);
            }
        }
    }

    class IgnoreAdapter extends RecyclerView.Adapter<IgnoreAdapter.IgnoreViewHolder> {

        private List<IgnoreItem> mData;

        IgnoreAdapter() {
            mData = new ArrayList<>();
        }

        void setData(List<IgnoreItem> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public IgnoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_ignore, parent, false);
            return new IgnoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IgnoreViewHolder holder, int position) {
            IgnoreItem item = mData.get(position);
            holder.mCreated.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date(item.mCreated)));
            holder.mName.setText(item.mName);
            Glide.with(getApplicationContext())
                    .load(AppUtil.getPackageIcon(getApplicationContext(), item.mPackageName))
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.mIcon);
            holder.setOnClickListener(item);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class IgnoreViewHolder extends RecyclerView.ViewHolder {

            private ImageView mIcon;
            private TextView mName;
            private TextView mCreated;

            IgnoreViewHolder(View itemView) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.app_image);
                mName = itemView.findViewById(R.id.app_name);
                mCreated = itemView.findViewById(R.id.app_time);
            }

            void setOnClickListener(final IgnoreItem item) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DbIgnoreExecutor.getInstance().deleteItem(item);
                        new MyAsyncTask(IgnoreActivity.this).execute();
                    }
                });
            }
        }
    }
    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ignore_list);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            supportFinishAfterTransition();
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.screenlocker.secure.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.contactSupport.ChatActivity;
import com.liveClientChat.LiveClientChatActivity;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;
import com.secureMarket.SecureMarketActivity;
import com.secureSetting.SecureSettingsMain;
import com.simplemobiletools.filemanager.pro.activities.MainActivity;

import java.util.List;
import java.util.stream.IntStream;

import static android.content.Intent.ACTION_VIEW;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.NUMBER_OF_NOTIFICATIONS;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    public List<AppInfo> appsList;
    private Context context;
    private ClearCacheListener listener;
    private PrefUtils prefUtils;


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView textView, numberOfNotifications;
        final ImageView img;
        final FrameLayout badge;

        ViewHolder(View itemView) {
            super(itemView);

            //Finds the views from our row.xml
            textView = itemView.findViewById(R.id.text);
            img = itemView.findViewById(R.id.img);
            badge = itemView.findViewById(R.id.badge_container);
            numberOfNotifications = itemView.findViewById(R.id.number_of_notifications);


            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(final View v) {
            final Context context = v.getContext();
            AppInfo info;
            try {
                info = appsList.get(getAdapterPosition());
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            if (prefUtils.getStringPref( CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
                String unique = info.getUniqueName();

                switch (unique) {
                    case AppConstants.SECURE_SETTINGS_UNIQUE:
                        Intent i = new Intent(context, SettingsActivity.class);
                        i.putExtra("show_default", "show_default");
                        context.startActivity(i);
                        ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        break;
                    case AppConstants.LIVE_CLIENT_CHAT_UNIQUE:
                        context.startActivity(new Intent(context, LiveClientChatActivity.class));
                        ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        break;
                    case BuildConfig.APPLICATION_ID:
                        Intent launch = new Intent(context, SettingsActivity.class);
                        context.startActivity(launch);
                        ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        break;
                }


                return;
            }
            if (info.isEnable()) {
                try {

                    String unique = info.getUniqueName();

                    switch (unique) {
                        case AppConstants.SECURE_SETTINGS_UNIQUE:
                            Intent i = new Intent(context, SettingsActivity.class);
                            i.putExtra("show_default", "show_default");
                            context.startActivity(i);
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case AppConstants.SECURE_CLEAR_UNIQUE:
                            showCacheDialog();
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case AppConstants.SECURE_MARKET_UNIQUE:
                            Intent intent = new Intent(context, SecureMarketActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case AppConstants.SUPPORT_UNIQUE:
                            context.startActivity(new Intent(context, ChatActivity.class));
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case AppConstants.SFM_UNIQUE:
                            context.startActivity(new Intent(context, MainActivity.class));
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case AppConstants.LIVE_CLIENT_CHAT_UNIQUE:
                            context.startActivity(new Intent(context, LiveClientChatActivity.class));
                            break;
                        case BuildConfig.APPLICATION_ID:
                            Intent intent1 = new Intent(context, SettingsActivity.class);
                            intent1.setAction(ACTION_VIEW);
                            context.startActivity(intent1);
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        default: {
                            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(info.getPackageName());
                            if (launchIntent != null) {
                                launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            }
                            /*launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );*/
                            context.startActivity(launchIntent);
                            ((Activity) context).overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        }
                    }


                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (!BuildConfig.APPLICATION_ID.equals(info.getPackageName()))
                        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> MyAppDatabase.getInstance(context).getDao().deleteOne(info.getUniqueName()));

                }

            } else {
                Toast.makeText(context, context.getResources().getString(R.string.app_disabled), Toast.LENGTH_SHORT).show();
            }


        }

    }


    private void showCacheDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getResources().getString(R.string.clear_cache_title));
        alertDialog.setIcon(android.R.drawable.stat_sys_warning);

        alertDialog.setMessage(context.getResources().getString(R.string.clear_cache_message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.ok_capital), (dialog, which) -> listener.clearCache(context));


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel_capital),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    RAdapter(Context context, PrefUtils prefUtils) {

        this.context = context;
        if (context instanceof ClearCacheListener) {
            listener = (ClearCacheListener) context;
        }
        this.prefUtils = prefUtils;

    }

    @Override
    public void onBindViewHolder(@NonNull RAdapter.ViewHolder viewHolder, int i) {

        //Here we use the information in the list we created to define the views

        AppInfo info = appsList.get(i);

        // Drawable appIcon = appsList.get(i).getIcon();

        viewHolder.badge.setVisibility(View.GONE);

        TextView textView = viewHolder.textView;
        switch (info.getLabel()) {

            case "Secure Market":
                textView.setText(context.getResources().getString(R.string.secure_market_title));

                break;
            case "Secure Clear":
                textView.setText(context.getResources().getString(R.string.secure_clear_title));

                break;
            case "Secure Settings":
                textView.setText(context.getResources().getString(R.string.secure_settings_activity_title));

                break;
            case "Live Chat Support":
                textView.setText(context.getResources().getString(R.string.live_client_device_id));
//
                int number = prefUtils.getIntegerPref( NUMBER_OF_NOTIFICATIONS);
                if (number > 0) {
                    viewHolder.badge.setVisibility(View.VISIBLE);
                    viewHolder.numberOfNotifications.setText( String.valueOf(number));
                } else {
                    viewHolder.badge.setVisibility(View.GONE);
                }

                break;
            default:
                textView.setText(info.getLabel());
                break;
        }
//        if (appsList.get(i).isNotification()){
//            viewHolder.badge.setVisibility(View.VISIBLE);
//        }
        //  ImageView imageView = viewHolder.img;
        // imageView.setImageDrawable(appIcon);

        Glide.with(viewHolder.img.getContext())
                .load(appsList.get(i).getIcon())
                .apply(new RequestOptions().centerCrop())
                .into(viewHolder.img);
        if (info.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            if (info.getNumberOfnotifications() == 0) {
                viewHolder.badge.setVisibility(View.GONE);
            } else {
                viewHolder.badge.setVisibility(View.VISIBLE);
                viewHolder.numberOfNotifications.setText(String.valueOf(info.getNumberOfnotifications()));
                Log.d("TEZST: 1 ", String.valueOf(info.getNumberOfnotifications()));
            }
        }
    }


    @Override
    public int getItemCount() {

        return appsList.size();
    }

    @NonNull
    @Override
    public RAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.row, parent, false);

//        context = parent.getContext();
        return new ViewHolder(view);
    }

    public void updateNotificationBadgeOfSL(int notification) {
        int index = IntStream.range(0, appsList.size())
                .filter(i -> appsList.get(i).getPackageName().equals(AppConstants.SECURE_SETTINGS_PACKAGE))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            appsList.get(index).setNumberOfnotifications(notification);
            notifyItemChanged(index);
        }
    }

    public interface ClearCacheListener {
        void clearCache(Context context);
    }
}
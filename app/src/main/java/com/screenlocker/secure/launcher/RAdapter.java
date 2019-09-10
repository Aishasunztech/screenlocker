package com.screenlocker.secure.launcher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureMarket.SecureMarketActivity;
import com.secureSetting.SecureSettingsMain;
import com.simplemobiletools.filemanager.pro.activities.MainActivity;

import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    public List<AppInfo> appsList;
    private Context context;
    private ClearCacheListener listener;

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView textView;
        final ImageView img;


        ViewHolder(View itemView) {
            super(itemView);

            //Finds the views from our row.xml
            textView = itemView.findViewById(R.id.text);
            img = itemView.findViewById(R.id.img);

            itemView.setOnClickListener(this);

//            itemView.setOnLongClickListener(v -> {
//                PrefUtils.saveBooleanPref(context, IS_SETTINGS_ALLOW, true);
//                openAppInfo(context, appsList.get(getAdapterPosition()).getPackageName());
//                return false;
//            });

        }


//        private void openAppInfo(Context context, String packageName) {
//
//            try {
//                if (!packageName.equals(context.getPackageName())) {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    intent.setData(Uri.parse("package:" + packageName));
//                    context.startActivity(intent);
//                }
//
//            } catch (Exception e) {
//                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//
//
//        }

        @Override
        public void onClick(final View v) {
            final Context context = v.getContext();
            AppInfo info;

            try {
                info = appsList.get(getAdapterPosition());
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }


            if (info.getPackageName().equals(context.getPackageName())) {
                PrefUtils.saveBooleanPref(context, IS_SETTINGS_ALLOW, true);
            }


            if (PrefUtils.getStringPref(context, CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
                String unique = info.getUniqueName();

                switch (unique) {
                    case AppConstants.SECURE_SETTINGS_UNIQUE:
                        Intent i = new Intent(context, SecureSettingsMain.class);
                        i.putExtra("show_default", "show_default");
                        context.startActivity(i);
                        break;
                    case AppConstants.SUPPORT_UNIQUE:
                        context.startActivity(new Intent(context, ChatActivity.class));
                        break;
                    case BuildConfig.APPLICATION_ID:
                        Intent launch = new Intent(context, SettingsActivity.class);
                        launch.setAction(ACTION_VIEW);
                        context.startActivity(launch);
                        break;
                }


                return;
            }


            if (info.isEnable()) {
                try {

                    String unique = info.getUniqueName();

                    switch (unique) {
                        case AppConstants.SECURE_SETTINGS_UNIQUE:
                            Intent i = new Intent(context, SecureSettingsMain.class);

                            if (PrefUtils.getStringPref(context, CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
                                i.putExtra("show_default", "show_default");
                            }
                            context.startActivity(i);
                            break;
                        case AppConstants.SECURE_CLEAR_UNIQUE:
                            showCacheDialog();
                            break;
                        case AppConstants.SECURE_MARKET_UNIQUE:
                            Intent intent = new Intent(context, SecureMarketActivity.class);
                            context.startActivity(intent);
                            break;
                        case AppConstants.SUPPORT_UNIQUE:
                            context.startActivity(new Intent(context, ChatActivity.class));
                            break;
                        case AppConstants.SFM_UNIQUE:
                            context.startActivity(new Intent(context, MainActivity.class));
                            break;
                        case BuildConfig.APPLICATION_ID:
                            Intent launch = new Intent(context, SettingsActivity.class);
                            launch.setAction(ACTION_VIEW);
                            context.startActivity(launch);
                            break;
                        case AppConstants.LIVE_CLIENT_CHAT_UNIQUE:
                            Intent intent2 = new Intent(context, LiveClientChatActivity.class);
                            context.startActivity(intent2);
                            break;
                        default: {
                            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(info.getPackageName());
//                        launchIntent.setAction(Intent.ACTION_VIEW);
                            if (launchIntent != null) {
                                launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                            }
                            /*launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );*/
                            context.startActivity(launchIntent);
                            break;
                        }
                    }


                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(context, "App not found", Toast.LENGTH_LONG).show();
//                    if (!BuildConfig.APPLICATION_ID.equals(info.getPackageName()))
//                        new Thread(() -> MyApplication.getAppDatabase(context).getDao().deleteOne(info.getUniqueName())).start();
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

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.ok_capital), (dialog, which) -> {
            listener.clearCache(context);


        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel_capital),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    RAdapter(Context context) {

        this.context = context;
        if (context instanceof ClearCacheListener) {
            listener = (ClearCacheListener) context;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RAdapter.ViewHolder viewHolder, int i) {

        //Here we use the information in the list we created to define the views

        String appLabel = appsList.get(i).getLabel();

        // Drawable appIcon = appsList.get(i).getIcon();

        TextView textView = viewHolder.textView;
        switch (appLabel) {

            case "Secure Market":
                textView.setText(context.getResources().getString(R.string.secure_market_title));

                break;
            case "Secure Clear":
                textView.setText(context.getResources().getString(R.string.secure_clear_title));

                break;
            case "Secure Settings":
                textView.setText(context.getResources().getString(R.string.secure_settings_activity_title));

                break;
            case "Contact Support":
                textView.setText(context.getResources().getString(R.string.contact_support_chat));
                break;
            default:
                textView.setText(appLabel);
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


    public interface ClearCacheListener {
        void clearCache(Context context);
    }
}
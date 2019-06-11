package com.screenlocker.secure.launcher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.contactSupport.ChatActivity;
import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.secureMarket.SecureMarketActivity;
import com.secureSetting.SecureSettingsMain;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    public List<AppInfo> appsList;
    private Context context;
    private ClearCacheListener listener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView textView;
        final ImageView img;

        ViewHolder(View itemView) {
            super(itemView);

            //Finds the views from our row.xml
            textView = itemView.findViewById(R.id.text);
            img = itemView.findViewById(R.id.img);

            img.setOnClickListener(this);
        }


        @Override
        public void onClick(final View v) {
            final Context context = v.getContext();
            AppInfo info = appsList.get(getAdapterPosition());
            if (info.isEnable()) {
                try {

                    String unique = info.getUniqueName();

                    switch (unique) {
                        case AppConstants.SECURE_SETTINGS_UNIQUE:
                            Intent i = new Intent(context, SecureSettingsMain.class);
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
                }

            } else {
                Toast.makeText(context, "App is disabled", Toast.LENGTH_SHORT).show();
            }



            /*new AsyncTask<Integer, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Integer... pos) {
                    AppInfo appInfo = MyApplication
                            .getAppDatabase(context)
                            .getDao()
                            .getParticularApp(appsList.get(pos[0]).getPackageName() + appsList.get(pos[0]).getLabel());
                    if (appInfo == null) {
                        return false;
                    } else {
                        return appInfo.isEnable();
                    }

                }

                @Override
                protected void onPostExecute(Boolean presentApp) {
                    super.onPostExecute(presentApp);

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getAdapterPosition());*/

        }

    }


    private void showCacheDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Clear Cache");
        alertDialog.setIcon(android.R.drawable.stat_sys_warning);

        alertDialog.setMessage("Proceed with clearing cache?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            listener.clearCache(context);


        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
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
        textView.setText(appLabel);
        //  ImageView imageView = viewHolder.img;
        // imageView.setImageDrawable(appIcon);

        Glide.with(viewHolder.img.getContext())
                .load(appsList.get(i).getIcon())
                .apply(new RequestOptions().centerCrop())
                .into(viewHolder.img);
    }


    @Override
    public int getItemCount() {

        //This method needs to be overridden so that Androids knows how many items
        //will be making it into the list

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
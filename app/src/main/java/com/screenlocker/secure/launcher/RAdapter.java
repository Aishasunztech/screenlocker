package com.screenlocker.secure.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;

import java.util.List;

public class RAdapter extends RecyclerView.Adapter<RAdapter.ViewHolder> {
    public List<AppInfo> appsList;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView textView;
        final ImageView img;


        //This is the subclass ViewHolder which simply

        //'holds the views' for us to show on each row
        ViewHolder(View itemView) {
            super(itemView);

            //Finds the views from our row.xml
            textView = itemView.findViewById(R.id.text);
            img = itemView.findViewById(R.id.img);

            img.setOnClickListener(this);
        }

        @SuppressLint("StaticFieldLeak")
        @Override
        public void onClick(final View v) {
            final Context context = v.getContext();

            new AsyncTask<Integer, Void, Boolean>() {
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
                    if (presentApp) {
                        try {
                            if (appsList.get(getAdapterPosition()).isExtension()){
                                Intent intent = new Intent(context , Class.forName(appsList.get(getAdapterPosition()).getPackageName()));
                                context.startActivity(intent);
                            }else {
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appsList.get(getAdapterPosition()).getPackageName());
                                context.startActivity(launchIntent);
                            }

                        } catch (Exception e) {
                            Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context, "App is disabled", Toast.LENGTH_SHORT).show();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getAdapterPosition());

        }

    }


    public RAdapter() {

//        this.context = context;
    }

    @Override
    public void onBindViewHolder(@NonNull RAdapter.ViewHolder viewHolder, int i) {

        //Here we use the information in the list we created to define the views

        String appLabel = appsList.get(i).getLabel();
        String appPackage = appsList.get(i).getPackageName();
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
}
package com.screenlocker.secure.settings.Wallpaper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.screenlocker.secure.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperVieHolder> {

    private OnClickListner mListner;
    public interface OnClickListner{
        void onItemClick(int position);
    }
    List<Integer> rawIds;
    public WallpaperAdapter(List<Integer> ids, OnClickListner listener) {
        rawIds = ids;
        mListner = listener;

    }

    @NonNull
    @Override
    public WallpaperVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.wallpaper_item_layout, parent, false);

//        context = parent.getContext();
        return new WallpaperVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperVieHolder holder, int position) {
        holder.wallpaper.setImageResource(rawIds.get(position));
        holder.wallpaper.setOnClickListener(v ->{
            mListner.onItemClick(position);
        });

    }

    @Override
    public int getItemCount() {
        return rawIds.size();
    }

    public class WallpaperVieHolder extends RecyclerView.ViewHolder{
        ImageView wallpaper;

        public WallpaperVieHolder(@NonNull View itemView) {
            super(itemView);
            wallpaper = itemView.findViewById(R.id.wallpaper);
        }
    }
}

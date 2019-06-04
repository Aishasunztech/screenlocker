

package com.stfalcon.chatkit.commons;

import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * Callback for implementing images loading in message list
 */
public interface ImageLoader {

    void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload);

}

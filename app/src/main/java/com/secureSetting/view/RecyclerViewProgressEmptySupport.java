package com.secureSetting.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Custom RecyclerView with support for showing another view when there's no data and another
 * one for loading.
 */
public class RecyclerViewProgressEmptySupport extends RecyclerView {

    /**
     * The view to show if the list is empty.
     */
    private View emptyView;

    /**
     * Observer for list data. Sets the empty view if the list is empty.
     */
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null) {
                if (adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    RecyclerViewProgressEmptySupport.this.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    RecyclerViewProgressEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }

        }
    };

    /**
     * View shown during loading.
     */
    private ProgressBar progressView;

    /**
     * @see RecyclerView#RecyclerView(Context)
     */
    public RecyclerViewProgressEmptySupport(Context context) {
        super(context);
    }

    /**
     * @see RecyclerView#RecyclerView(Context, AttributeSet)
     */
    public RecyclerViewProgressEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @see RecyclerView#RecyclerView(Context, AttributeSet, int)
     */
    public RecyclerViewProgressEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    /**
     * Sets the empty view.
     *
     * @param emptyView the {@link #emptyView}
     */
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    /**
     * Sets the progress view.
     *
     * @param progressView the {@link #progressView}.
     */
    public void setProgressView(ProgressBar progressView) {
        this.progressView = progressView;
    }

    /**
     * Shows the progress view.
     */
    public void startLoading() {
        // Hides the empty view.
        if (this.emptyView != null) {
            this.emptyView.setVisibility(GONE);
        }
        // Shows the progress bar.
        if (this.progressView != null) {
            this.progressView.setVisibility(VISIBLE);
        }
    }

    /**
     * Hides the progress view.
     */
    public void endLoading() {
        // Hides the progress bar.
        if (this.progressView != null) {
            this.progressView.setVisibility(GONE);
        }

        // Forces the view refresh.
        emptyObserver.onChanged();
    }
}
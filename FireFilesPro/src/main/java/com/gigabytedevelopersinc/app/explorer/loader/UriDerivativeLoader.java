package com.gigabytedevelopersinc.app.explorer.loader;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;

import java.io.Closeable;

import com.gigabytedevelopersinc.app.explorer.misc.AsyncTaskLoader;

/**
 * Loader that derives its data from a Uri. Watches for {@link ContentObserver}
 * changes while started, manages {@link CancellationSignal}, and caches
 * returned results.
 */
public abstract class UriDerivativeLoader<P, R> extends AsyncTaskLoader<R> {
    final ForceLoadContentObserver mObserver;

    private final P mParam;

    private R mResult;
    private CancellationSignal mCancellationSignal;

	private boolean mCancelled;

    @Override
    public final R loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled2()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            return loadInBackground(mParam, mCancellationSignal);
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    private boolean isLoadInBackgroundCanceled2() {
		return mCancelled;
	}

	public abstract R loadInBackground(P param, CancellationSignal signal);

    public void cancelLoadInBackground2() {

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    @Override
    public void deliverResult(R result) {
        if (isReset()) {
            closeQuietly(result);
            return;
        }
        R oldResult = mResult;
        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldResult != null && oldResult != result) {
            closeQuietly(oldResult);
        }
    }

    public UriDerivativeLoader(Context context, P param) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mParam = param;
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(R result) {
    	mCancelled = true;
    	cancelLoadInBackground2();
        closeQuietly(result);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        closeQuietly(mResult);
        mResult = null;

        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void closeQuietly(R result) {
        if (result instanceof Closeable) {
            try {
                ((Closeable) result).close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
        else if (result instanceof AutoCloseable) {
            try {
                ((AutoCloseable) result).close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
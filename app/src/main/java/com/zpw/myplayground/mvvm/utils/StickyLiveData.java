package com.zpw.myplayground.mvvm.utils;


import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import static com.fwk.sdk.utils.SdkLogUtils.TAG_FWK;

public class StickyLiveData<T> extends LiveData<T> {

    private static final String TAG = TAG_FWK + StickyLiveData.class.getSimpleName();

    private IDestroyListener mDestroyListener;

    private final String mChannelName;
    private T mStickyData;
    private int mVersion = 0;

    public StickyLiveData(String channelName) {
        mChannelName = channelName;
    }

    @Override
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        mVersion++;
        super.postValue(value);
    }

    public void setDestroyListener(IDestroyListener destroyListener) {
        this.mDestroyListener = destroyListener;
    }

    public void setStickyData(T stickyData) {
        this.mStickyData = stickyData;
        setValue(stickyData);
    }

    public void postStickyData(T stickyData) {
        this.mStickyData = stickyData;
        postValue(stickyData);
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observerSticky(owner, observer, false);
    }

    /**
     * Sticky event  observer.
     *
     * @param owner    LifecycleOwner
     * @param observer LiveData observer
     * @param sticky   isSticky
     */
    public void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean sticky) {
        super.observe(owner, new WrapperObserver<>(this, observer, sticky));
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source,
                                       @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    if (mDestroyListener != null) {
                        mDestroyListener.onLiveDataDestroy(mChannelName);
                    }
                }
            }
        });
    }

    private static class WrapperObserver<T> implements Observer<T> {
        private final StickyLiveData<T> mLiveData;
        private final Observer<T> mObserver;
        private final boolean mSticky;
        private int mLastVersion;

        public WrapperObserver(StickyLiveData liveData, Observer<T> observer, boolean sticky) {
            mLiveData = liveData;
            mObserver = observer;
            mSticky = sticky;
            mLastVersion = mLiveData.mVersion;
        }

        @Override
        public void onChanged(T t) {
            if (mLastVersion >= mLiveData.mVersion) {
                if (mSticky && mLiveData.mStickyData != null) {
                    mObserver.onChanged(mLiveData.mStickyData);
                }
                return;
            }
            mLastVersion = mLiveData.mVersion;
            mObserver.onChanged(t);
        }
    }

    public interface IDestroyListener {

        /**
         * Call back when liveData destroy.
         *
         * @param channelName channel name
         */
        void onLiveDataDestroy(String channelName);

    }
}

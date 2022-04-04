package com.zpw.myplayground.aidl.listener;

public interface IServiceConnectListener {

    default void onServiceConnected() {
    }

    default void onServiceDisconnected() {
    }

    default void onBindServiceResult(boolean result) {
    }

    default void onUnbindService() {
    }

    default void onBinderDied() {
    }

}

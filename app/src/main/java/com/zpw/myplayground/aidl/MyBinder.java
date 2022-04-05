package com.zpw.myplayground.aidl;


import android.os.RemoteException;

import com.zpw.myplayground.IMyAidlInterface;

public class MyBinder extends IMyAidlInterface.Stub {

    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }
}

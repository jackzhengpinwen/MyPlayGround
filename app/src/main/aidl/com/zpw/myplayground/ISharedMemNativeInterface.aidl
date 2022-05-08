// ISharedMemNativeInterface.aidl
package com.zpw.myplayground;

import android.os.ParcelFileDescriptor;

interface ISharedMemNativeInterface {
    ParcelFileDescriptor openSharedMem(String name, int size, boolean create);
}
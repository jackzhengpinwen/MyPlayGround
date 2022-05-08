// IAshmemInterface.aidl
package com.zpw.myplayground;

interface IAshmemInterface {
    void serverToClient(in ParcelFileDescriptor pfd);
}
#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/mman.h>

#define ASHMEM_NAME_LEN         256
#define __ASHMEMIOC             0x77
#define ASHMEM_SET_NAME _IOW(__ASHMEMIOC, 1, char[ASHMEM_NAME_LEN])
#define ASHMEM_SET_SIZE _IOW(__ASHMEMIOC, 3, size_t)

struct memArea{
    int *map;
    int fd;
    int size;
};

struct memArea maps[10];
int num = 0;

extern "C"
JNIEXPORT jint JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_getFD(
        JNIEnv *env, jobject cl, jstring path, jint size) {
    const char *name = env->GetStringUTFChars(path, NULL);
    jint fd = open("/dev/ashmem", O_RDWR);
    ioctl(fd, ASHMEM_SET_NAME, name);
    ioctl(fd, ASHMEM_SET_SIZE, size);
    maps[num].size = size;
    maps[num].fd = fd;
    maps[num++].map = (int *) mmap(0, size, PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0);
    env->ReleaseStringUTFChars(path, name);
    return fd;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_setVal(
        JNIEnv *env, jobject thiz, jint fd, jint pos, jint val) {
    for(int i = 0; i < num; i++){
        if(maps[i].fd == fd) {
            if(pos < (maps[i].size/ sizeof(int))) {
                maps[i].map[pos] = num;
                return 0;
            }
            return -1;
        }
    }
    return -1;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_getVal(
        JNIEnv *env, jobject thiz, jint fd, jint pos) {
    for(int i = 0; i < num; i++) {
        if(maps[i].fd == fd) {
            if(pos < (maps[i].size/ sizeof(int))) {
                return maps[i].map[pos];
            }
            return -1;
        }
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zpw_myplayground_jni_JniActivity_helloFromCXX(JNIEnv *env, jobject thiz) {

}

int *map ;
int size;

extern "C"
JNIEXPORT jint JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeShareMemoryActivity_setVal(
        JNIEnv *env, jobject thiz, jint pos, jint val) {
    if(pos < (size/ sizeof(int))) {
        map[pos] = num;
        return 0;
    }
    return -1;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeShareMemoryActivity_getVal(
        JNIEnv *env, jobject thiz, jint pos) {
    if(pos < (size/ sizeof(int))) {
        return map[pos];
    }
    return -1;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_zpw_myplayground_sharememory_native_NativeShareMemoryActivity_setMap(
        JNIEnv *env, jobject thiz, jint fd, jint size) {
    size = size;
    map = (int *)mmap(0,size,PROT_READ|PROT_WRITE,MAP_SHARED,fd,0);
}

static JNINativeMethod method_table[] = {
        { "setVal", "(III)I", (void *) Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_setVal },
        { "getVal", "(II)I", (void *) Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_getVal },
        { "getFD", "(Ljava/lang/String;I)I", (void *)Java_com_zpw_myplayground_sharememory_native_NativeAshmemService_getFD }
};

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass clazz = env->FindClass("com/zpw/myplayground/sharememory/native/NativeAshmemService");
        if (clazz) {
            jint ret = env->RegisterNatives(clazz, method_table, sizeof(method_table) / sizeof(method_table[0]));
            env->DeleteLocalRef(clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        } else {
            return JNI_ERR;
        }
    }
}
#include <jni.h>
#include <iostream>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_filterproject_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "String Teste nosso";
    return env->NewStringUTF(hello.c_str());
};

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_filterproject_MainActivity_test(JNIEnv *env, jobject thisObject) {
    jfloatArray returnArray = (env)->NewFloatArray(2);
    jfloat array1[2];
    array1[0] = 2.0;
    array1[1] = 4.1;
    env->SetFloatArrayRegion(returnArray, 0, 2, array1);
    return returnArray;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_example_filterproject_MainActivity_amplify(JNIEnv *env, jobject thisObject, jfloatArray input, int bufferSize) {
    jfloatArray returnArray = input;
    jfloat *array = env->GetFloatArrayElements(returnArray, NULL);
    for(int i=0; i < bufferSize; i++){
        array[i]=array[i]*0.2;
    }
    env->SetFloatArrayRegion(returnArray, 0, bufferSize, array);
    return returnArray;
}
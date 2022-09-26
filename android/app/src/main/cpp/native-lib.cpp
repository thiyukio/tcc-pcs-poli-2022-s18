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
Java_com_example_filterproject_MainActivity_amplify(JNIEnv *env, jobject thisObject, jfloatArray input, jfloatArray input2, jfloatArray haux, int bufferSize) {
    jfloatArray returnArray = input;
    jfloat *array = env->GetFloatArrayElements(returnArray, NULL);
    jfloatArray returnArray2 = input2;
    jfloat *array2 = env->GetFloatArrayElements(returnArray2, NULL);
    jfloatArray aux1 = env->NewFloatArray(2*bufferSize);
    jfloat *array_total = env->GetFloatArrayElements(aux1,NULL);
    jfloat *saida = env->GetFloatArrayElements(returnArray,NULL);
    jfloatArray aux2 = env->NewFloatArray(20);
    jfloatArray haux2 = haux;
    jfloat *h = env->GetFloatArrayElements(haux2,NULL);

    for(int i = 0; i < bufferSize; i++){
        array_total[i]=array2[i];
        array_total[i+bufferSize]=array[i];
    }
    for(int i=bufferSize; i < 2*bufferSize; i++){
        //array[i]=array[i]*0.2;
        for(int j=0; j < 20;j++){
            saida[i]=saida[i]+h[j]*saida[i-j];
        }
    }
    env->SetFloatArrayRegion(returnArray, 0, bufferSize, saida);
    return returnArray;
}
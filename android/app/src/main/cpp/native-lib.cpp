#include <jni.h>
#include <iostream>
#include <string>
#include <android/log.h>
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
    jfloat *array = env->GetFloatArrayElements(input, NULL);
    jfloat *array2 = env->GetFloatArrayElements(input2, NULL);
    jfloat janela[20];
    jfloat *h = env->GetFloatArrayElements(haux,NULL);
    jint final_aux = bufferSize-1;
    jfloat yaux =0.0f;
    jfloat saida[bufferSize];

    for(int i = 0; i < 19; i++){
        janela[i]=array2[bufferSize-19+i];
    }

    for(int i=0; i < bufferSize; i++){
        janela[19]=array[i];
        yaux=0.0f;
        for(int j=0; j < 20;j++){
            //__android_log_print(ANDROID_LOG_DEBUG, "TEST", strsignal(j));
            yaux=yaux+h[j]*janela[19-j];
        }
        saida[i] = yaux;
        for(int k = 0; k < 19; k++){
            janela[k]=janela[k+1];
        }
    }
    env->SetFloatArrayRegion(returnArray, 0, bufferSize, saida);
    return returnArray;
}
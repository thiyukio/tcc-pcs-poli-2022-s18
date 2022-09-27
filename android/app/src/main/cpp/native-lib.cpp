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
    jfloat *array = env->GetFloatArrayElements(input, NULL);
    jfloat *array2 = env->GetFloatArrayElements(input2, NULL);
    jfloat janela[20];
    jfloat *h = env->GetFloatArrayElements(haux,NULL);
    jint final_aux = bufferSize-1;
    jfloat yaux =0;
    jfloat saida[bufferSize];

    for(int i = 0; i < final_aux; i++){
        janela[i]=array2[i+1];
    }

    for(int i=0; i < bufferSize; i++){
        janela[final_aux]=array[i];
        for(int j=0; j < 20;j++){
            yaux=yaux+h[j]*janela[19-j];
        }
        saida[i] = yaux;
        for(int k = 0; k < 20; k++){
            janela[k]=janela[k+1];
        }
    }
    env->SetFloatArrayRegion(returnArray, 0, bufferSize, saida);
    return returnArray;
}
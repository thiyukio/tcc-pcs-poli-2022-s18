#include <jni.h>
#include <memory>

#include <android/asset_manager_jni.h>

#include "google/utils/logging.h"
#include "Filter.h"

//
// Created by EduardoTA on 13/09/2022.
//


extern "C" {

std::unique_ptr<Filter> filter;

JNIEXPORT void JNICALL
Java_com_example_filterproject_MainActivity_native_1onStart(JNIEnv *env,
                                                            jobject thiz,
                                                            jobject jAssetManager) {
    AAssetManager *assetManager = AAssetManager_fromJava(env, jAssetManager);
    if (assetManager == nullptr) {
        LOGE("Could not obtain the AAssetManager");
        return;
    }

    filter = std::make_unique<Filter>(*assetManager);
}

JNIEXPORT void JNICALL
Java_com_example_filterproject_MainActivity_native_1onStop(JNIEnv *env, jobject instance) {

    filter->stop();
}

JNIEXPORT void JNICALL
Java_com_example_filterproject_MainActivity_native_1setDefaultStreamValues(JNIEnv *env,
                                                                                    jclass type,
                                                                                    jint sampleRate,
                                                                                    jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}
JNIEXPORT void JNICALL
Java_com_example_filterproject_MainActivity_native_1onResume(JNIEnv *env, jobject thiz) {
    filter->start();
}
}
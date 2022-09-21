//
// Created by EduardoTA on 13/09/2022.
//

#ifndef FILTER_PROJECT_FILTER_H
#define FILTER_PROJECT_FILTER_H

#include <future>

#include <android/asset_manager.h>
#include <oboe/Oboe.h>

#include "google/audio/Mixer.h"

#include "google/audio/Player.h"
#include "google/audio/AAssetDataSource.h"
#include "utils/LockFreeQueue.h"
#include "utils/UtilityFunctions.h"
#include "Constants.h"

using namespace oboe;

class Filter : public AudioStreamDataCallback, AudioStreamErrorCallback {
public:
    explicit Filter(AAssetManager&);
    void start();
    void stop();

    // Inherited from oboe::AudioStreamDataCallback.
    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    // Inherited from oboe::AudioStreamErrorCallback.
    void onErrorAfterClose(AudioStream *oboeStream, Result error) override;

private:
    AAssetManager& mAssetManager;
    std::shared_ptr<AudioStream> mAudioStream;
    std::unique_ptr<Player> mPlayer;
    Mixer mMixer;

    std::atomic<int64_t> mCurrentFrame { 0 };

    std::future<void> mLoadingResult;

    void load();
    bool openStream();
    bool setupAudioSources();
    void scheduleSongEvents();
};


#endif //FILTER_PROJECT_FILTER_H

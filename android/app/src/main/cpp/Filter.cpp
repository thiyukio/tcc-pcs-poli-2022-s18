//
// Created by EduardoTA on 13/09/2022.
//

#include "google/utils/logging.h"
#include <thread>
#include <cinttypes>

#include "Filter.h"

Filter::Filter(int fd, int length): fd(fd), length(length) {
}

void Filter::load() {
    if (!openStream()) {
        return;
    }

    if (!setupAudioSources()) {
        return;
    }


    Result result = mAudioStream->requestStart();
    if (result != Result::OK){
        LOGE("Failed to start stream. Error: %s", convertToText(result));
        return;
    }
}

void Filter::start() {
    // async returns a future, we must store this future to avoid blocking. It's not sufficient
    // to store this in a local variable as its destructor will block until Game::load completes.
    mLoadingResult = std::async(&Filter::load, this);
}

void Filter::stop(){
    if (mAudioStream){
        mAudioStream->stop();
        mAudioStream->close();
        mAudioStream.reset();
    }
}

DataCallbackResult Filter::onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) {

    float *outputBuffer = static_cast<float *>(audioData);

    for (int i = 0; i < numFrames; ++i) {
        mMixer.renderAudio(outputBuffer+(oboeStream->getChannelCount()*i), 1);
        mCurrentFrame++;
    }


    return DataCallbackResult::Continue;
}

void Filter::onErrorAfterClose(AudioStream *audioStream, Result error) {
    if (error == Result::ErrorDisconnected){
        mAudioStream.reset();
        mMixer.removeAllTracks();
        mCurrentFrame = 0;
        start();
    } else {
        LOGE("Stream error: %s", convertToText(error));
    }
}

bool Filter::openStream() {

    // Create an audio stream
    AudioStreamBuilder builder;
    builder.setFormat(AudioFormat::Float);
    builder.setFormatConversionAllowed(true);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setSharingMode(SharingMode::Exclusive);
    builder.setSampleRate(48000);
    builder.setSampleRateConversionQuality(
            SampleRateConversionQuality::Medium);
    builder.setChannelCount(2);
    builder.setDataCallback(this);
    builder.setErrorCallback(this);
    Result result = builder.openStream(mAudioStream);
    if (result != Result::OK){
        LOGE("Failed to open stream. Error: %s", convertToText(result));
        return false;
    }

    mMixer.setChannelCount(mAudioStream->getChannelCount());

    return true;
}

bool Filter::setupAudioSources() {

    // Set the properties of our audio source(s) to match that of our audio stream
    AudioProperties targetProperties {
            .channelCount = mAudioStream->getChannelCount(),
            .sampleRate = mAudioStream->getSampleRate()
    };

    // Create a data source and player for the clap sound
//    std::shared_ptr<AAssetDataSource> mSource {
//            AAssetDataSource::newFromCompressedAsset(mAssetManager, kBackingTrackFilename, targetProperties)
//    };
    std::shared_ptr<AFileDataSource> mSource {
            AFileDataSource::newFromFile(fd, length, targetProperties)
    };
    if (mSource == nullptr){
        LOGE("Could not load source data for clap sound");
        return false;
    }
    mPlayer = std::make_unique<Player>(mSource);
    mPlayer->setPlaying(true);
    mPlayer->setLooping(true);

    // Add player to a mixer
    mMixer.addTrack(mPlayer.get());

    return true;
}
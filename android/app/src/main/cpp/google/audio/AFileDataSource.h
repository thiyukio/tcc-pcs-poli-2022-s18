/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef RHYTHMGAME_AFILEDATASOURCE_H
#define RHYTHMGAME_AFILEDATASOURCE_H

#include <android/asset_manager.h>
#include "../../Constants.h"
#include "DataSource.h"

class AFileDataSource : public DataSource {

public:
    int64_t getSize() const override { return mBufferSize; }
    AudioProperties getProperties() const override { return mProperties; }
    const float* getData() const override { return mBuffer.get(); }

    static AFileDataSource* newFromFile(
            int fd, int length,
            AudioProperties targetProperties);

private:

    AFileDataSource(std::unique_ptr<float[]> data, size_t size,
                     const AudioProperties properties)
            : mBuffer(std::move(data))
            , mBufferSize(size)
            , mProperties(properties) {
    }

    const std::unique_ptr<float[]> mBuffer;
    const int64_t mBufferSize;
    const AudioProperties mProperties;

};
#endif //RHYTHMGAME_AFILEDATASOURCE_H

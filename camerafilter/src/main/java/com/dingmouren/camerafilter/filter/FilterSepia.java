/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dingmouren.camerafilter.filter;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 * 应用简单的棕褐色效果。
 */
public class FilterSepia extends FilterColorMatrix {

    public FilterSepia() {
        this(1.0f);
    }

    public FilterSepia(final float intensity) {
        super(intensity, new float[] {
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f
        });
    }
}

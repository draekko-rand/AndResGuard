/*
 * Copyright (C) 2020 The Android Open Source Project
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

package apksig.internal.apk;

/**
 * Base implementation of a supported signature for an APK.
 */
public class ApkSupportedSignature {
    public final SignatureAlgorithm algorithm;
    public final byte[] signature;

    /**
     * Constructs a new supported signature using the provided {@code algorithm} and {@code
     * signature} bytes.
     */
    public ApkSupportedSignature(SignatureAlgorithm algorithm, byte[] signature) {
        this.algorithm = algorithm;
        this.signature = signature;
    }

}

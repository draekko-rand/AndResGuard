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
package apksig.internal.apk.stamp;

import static apksig.internal.apk.ApkSigningBlockUtils.encodeAsSequenceOfLengthPrefixedPairsOfIntAndLengthPrefixedBytes;
import static apksig.internal.apk.stamp.SourceStampConstants.V1_SOURCE_STAMP_BLOCK_ID;

import apksig.ApkVerifier;
import apksig.apk.ApkFormatException;
import apksig.apk.ApkUtils;
import apksig.internal.apk.ApkSigningBlockUtils;
import apksig.internal.apk.ContentDigestAlgorithm;
import apksig.internal.apk.SignatureInfo;
import apksig.internal.util.Pair;
import apksig.util.DataSource;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Source Stamp verifier.
 *
 * <p>V1 of the source stamp verifies the stamp signature of at most one signature scheme.
 */
public abstract class V1SourceStampVerifier {

    /** Hidden constructor to prevent instantiation. */
    private V1SourceStampVerifier() {}

    /**
     * Verifies the provided APK's SourceStamp signatures and returns the result of verification.
     * The APK must be considered verified only if {@link ApkSigningBlockUtils.Result#verified} is
     * {@code true}. If verification fails, the result will contain errors -- see {@link
     * ApkSigningBlockUtils.Result#getErrors()}.
     *
     * @throws NoSuchAlgorithmException if the APK's signatures cannot be verified because a
     *     required cryptographic algorithm implementation is missing
     * @throws ApkSigningBlockUtils.SignatureNotFoundException if no SourceStamp signatures are
     *     found
     * @throws IOException if an I/O error occurs when reading the APK
     */
    public static ApkSigningBlockUtils.Result verify(
            DataSource apk,
            ApkUtils.ZipSections zipSections,
            byte[] sourceStampCertificateDigest,
            Map<ContentDigestAlgorithm, byte[]> apkContentDigests,
            int minSdkVersion,
            int maxSdkVersion)
            throws IOException, NoSuchAlgorithmException,
                    ApkSigningBlockUtils.SignatureNotFoundException {
        ApkSigningBlockUtils.Result result =
                new ApkSigningBlockUtils.Result(ApkSigningBlockUtils.VERSION_SOURCE_STAMP);
        SignatureInfo signatureInfo =
                ApkSigningBlockUtils.findSignature(
                        apk, zipSections, V1_SOURCE_STAMP_BLOCK_ID, result);

        verify(
                signatureInfo.signatureBlock,
                sourceStampCertificateDigest,
                apkContentDigests,
                minSdkVersion,
                maxSdkVersion,
                result);
        return result;
    }

    /**
     * Verifies the provided APK's SourceStamp signatures and outputs the results into the provided
     * {@code result}. APK is considered verified only if there are no errors reported in the {@code
     * result}. See {@link #verify(DataSource, ApkUtils.ZipSections, byte[], Map, int, int)} for
     * more information about the contract of this method.
     */
    private static void verify(
            ByteBuffer sourceStampBlock,
            byte[] sourceStampCertificateDigest,
            Map<ContentDigestAlgorithm, byte[]> apkContentDigests,
            int minSdkVersion,
            int maxSdkVersion,
            ApkSigningBlockUtils.Result result)
            throws NoSuchAlgorithmException {
        ApkSigningBlockUtils.Result.SignerInfo signerInfo =
                new ApkSigningBlockUtils.Result.SignerInfo();
        result.signers.add(signerInfo);
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            ByteBuffer sourceStampBlockData =
                    ApkSigningBlockUtils.getLengthPrefixedSlice(sourceStampBlock);
            byte[] digestBytes =
                    encodeAsSequenceOfLengthPrefixedPairsOfIntAndLengthPrefixedBytes(
                            getApkDigests(apkContentDigests));
            SourceStampVerifier.verifyV1SourceStamp(
                    sourceStampBlockData,
                    certFactory,
                    signerInfo,
                    digestBytes,
                    sourceStampCertificateDigest,
                    minSdkVersion,
                    maxSdkVersion);
            result.verified = !result.containsErrors() && !result.containsWarnings();
        } catch (CertificateException e) {
            throw new IllegalStateException("Failed to obtain X.509 CertificateFactory", e);
        } catch (ApkFormatException | BufferUnderflowException e) {
            signerInfo.addWarning(ApkVerifier.Issue.SOURCE_STAMP_MALFORMED_SIGNATURE);
        }
    }

    private static List<Pair<Integer, byte[]>> getApkDigests(
            Map<ContentDigestAlgorithm, byte[]> apkContentDigests) {
        List<Pair<Integer, byte[]>> digests = new ArrayList<>();
        for (Map.Entry<ContentDigestAlgorithm, byte[]> apkContentDigest :
                apkContentDigests.entrySet()) {
            digests.add(Pair.of(apkContentDigest.getKey().getId(), apkContentDigest.getValue()));
        }
        Collections.sort(digests, Comparator.comparing(Pair::getFirst));
        return digests;
    }
}

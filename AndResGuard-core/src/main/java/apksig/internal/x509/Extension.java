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

package apksig.internal.x509;

import apksig.internal.asn1.Asn1Class;
import apksig.internal.asn1.Asn1Field;
import apksig.internal.asn1.Asn1Type;

import java.nio.ByteBuffer;

/**
 * X509 {@code Extension} as specified in RFC 5280.
 */
@Asn1Class(type = Asn1Type.SEQUENCE)
public class Extension {
    @Asn1Field(index = 0, type = Asn1Type.OBJECT_IDENTIFIER)
    public String extensionID;

    @Asn1Field(index = 1, type = Asn1Type.BOOLEAN, optional = true)
    public boolean isCritial = false;

    @Asn1Field(index = 2, type = Asn1Type.OCTET_STRING)
    public ByteBuffer extensionValue;
}

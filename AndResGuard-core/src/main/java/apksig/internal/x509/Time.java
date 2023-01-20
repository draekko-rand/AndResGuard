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

/**
 * {@code Time} as specified in RFC 5280.
 */
@Asn1Class(type = Asn1Type.CHOICE)
public class Time {

    @Asn1Field(type = Asn1Type.UTC_TIME)
    public String utcTime;

    @Asn1Field(type = Asn1Type.GENERALIZED_TIME)
    public String generalizedTime;
}

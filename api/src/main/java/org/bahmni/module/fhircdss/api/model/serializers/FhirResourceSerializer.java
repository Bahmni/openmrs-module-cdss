/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhircdss.api.model.serializers;

import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Resource;

import java.io.IOException;

public class FhirResourceSerializer extends StdSerializer<Resource> {
    public FhirResourceSerializer() {
        this(null);
    }

    public FhirResourceSerializer(Class clazz) {
        super(clazz);
    }

    @Override
    public void serialize(Resource resource, JsonGenerator jsonGenerator, SerializerProvider serializer)
            throws IOException {
        IParser parser = CdssUtils.getFhirJsonParser();
        String jsonStr = parser.encodeResourceToString(resource);
        jsonGenerator.writeRawValue(jsonStr);
    }
}
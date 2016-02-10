/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.ingest.attachment;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.ingest.core.AbstractProcessorFactory;
import org.elasticsearch.test.ESTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;

public class AttachmentProcessorFactoryTests extends ESTestCase {

    private AttachmentProcessor.Factory factory = new AttachmentProcessor.Factory();

    public void testBuildDefaults() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("source_field", "_field");

        String processorTag = randomAsciiOfLength(10);
        config.put(AbstractProcessorFactory.TAG_KEY, processorTag);

        AttachmentProcessor processor = factory.create(config);
        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getSourceField(), equalTo("_field"));
        assertThat(processor.getTargetField(), equalTo("attachment"));
        assertThat(processor.getFields(), sameInstance(AttachmentProcessor.Factory.DEFAULT_FIELDS));
    }

    public void testConfigureIndexedChars() throws Exception {
        int indexedChars = randomIntBetween(1, 100000);
        Map<String, Object> config = new HashMap<>();
        config.put("source_field", "_field");
        config.put("indexed_chars", indexedChars);

        String processorTag = randomAsciiOfLength(10);
        config.put(AbstractProcessorFactory.TAG_KEY, processorTag);
        AttachmentProcessor processor = factory.create(config);
        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getIndexedChars(), is(indexedChars));
    }

    public void testBuildTargetField() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("source_field", "_field");
        config.put("target_field", "_field");
        AttachmentProcessor processor = factory.create(config);
        assertThat(processor.getSourceField(), equalTo("_field"));
        assertThat(processor.getTargetField(), equalTo("_field"));
    }

    public void testBuildFields() throws Exception {
        Set<AttachmentProcessor.Field> fields = EnumSet.noneOf(AttachmentProcessor.Field.class);
        List<String> fieldNames = new ArrayList<>();
        int numFields = scaledRandomIntBetween(1, AttachmentProcessor.Field.values().length);
        for (int i = 0; i < numFields; i++) {
            AttachmentProcessor.Field field = AttachmentProcessor.Field.values()[i];
            fields.add(field);
            fieldNames.add(field.name().toLowerCase(Locale.ROOT));
        }
        Map<String, Object> config = new HashMap<>();
        config.put("source_field", "_field");
        config.put("fields", fieldNames);
        AttachmentProcessor processor = factory.create(config);
        assertThat(processor.getSourceField(), equalTo("_field"));
        assertThat(processor.getFields(), equalTo(fields));
    }

    public void testBuildIllegalFieldOption() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("source_field", "_field");
        config.put("fields", Collections.singletonList("invalid"));
        try {
            factory.create(config);
            fail("exception expected");
        } catch (ElasticsearchParseException e) {
            assertThat(e.getMessage(), equalTo("[fields] illegal field option [invalid]. valid values are " +
                "[CONTENT, TITLE, NAME, AUTHOR, KEYWORDS, DATE, CONTENT_TYPE, CONTENT_LENGTH, LANGUAGE]"));
        }

        config = new HashMap<>();
        config.put("source_field", "_field");
        config.put("fields", "invalid");
        try {
            factory.create(config);
            fail("exception expected");
        } catch (ElasticsearchParseException e) {
            assertThat(e.getMessage(), equalTo("[fields] property isn't a list, but of type [java.lang.String]"));
        }
    }
}

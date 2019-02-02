/*
 * Copyright 2018-2019 the Justify authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.leadpony.justify.internal.keyword.combiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.ObjectKeyword;

/**
 * Skeletal implementation for "properties" and "patternProperties" keywords.
 *
 * @author leadpony
 */
public abstract class AbstractProperties<K> extends Combiner implements ObjectKeyword {

    protected final Map<K, JsonSchema> propertyMap;
    private JsonSchema defaultSchema;

    protected AbstractProperties() {
        this.propertyMap = new LinkedHashMap<>();
        this.defaultSchema = AdditionalProperties.DEFAULT.getSubschema();
    }

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new PropertiesEvaluator(defaultSchema);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new NegatedPropertiesEvaluator(defaultSchema);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonObjectBuilder propertiesBuilder = builderFactory.createObjectBuilder();
        propertyMap.forEach((key, value)->propertiesBuilder.add(key.toString(), value.toJson()));
        builder.add(name(), propertiesBuilder.build());
    }

    @Override
    public void addToEvaluatables(List<Keyword> evaluatables, Map<String, Keyword> keywords) {
        if (keywords.containsKey("additionalProperties")) {
            AdditionalProperties additionalProperties = (AdditionalProperties)keywords.get("additionalProperties");
            this.defaultSchema = additionalProperties.getSubschema();
        }
    }

    @Override
    public boolean hasSubschemas() {
        return !propertyMap.isEmpty();
    }

    @Override
    public Stream<JsonSchema> subschemas() {
        return propertyMap.values().stream();
    }

    public void addProperty(K key, JsonSchema subschema) {
        propertyMap.put(key, subschema);
    }

    protected abstract void findSubschemasFor(String keyName, Collection<JsonSchema> subschemas);

    protected class PropertiesEvaluator extends AbstractConjunctivePropertiesEvaluator {

        private final JsonSchema defaultSchema;
        private final List<JsonSchema> subschemas = new ArrayList<>();

        PropertiesEvaluator(JsonSchema defaultSchema) {
            this.defaultSchema = defaultSchema;
        }

        @Override
        public void updateChildren(Event event, JsonParser parser) {
            if (event == Event.KEY_NAME) {
                processKeyName(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                appendEvaluators(type);
            }
        }

        protected void processKeyName(String keyName) {
            findSubschemasFor(keyName, subschemas);
            if (subschemas.isEmpty()) {
                findDefaultSchemaFor(keyName);
            } else if (subschemas.contains(JsonSchema.FALSE)) {
                appendRedundantPropertyEvaluator(keyName);
                subschemas.clear();
            }
        }

        private void findDefaultSchemaFor(String keyName) {
            JsonSchema subschema = this.defaultSchema;
            if (subschema == JsonSchema.FALSE) {
                appendRedundantPropertyEvaluator(keyName);
            } else {
                subschemas.add(subschema);
            }
        }

        private void appendEvaluators(InstanceType type) {
            for (JsonSchema subschema : this.subschemas) {
                append(subschema.createEvaluator(type));
            }
            this.subschemas.clear();
        }

        private void appendRedundantPropertyEvaluator(String keyName) {
            append(new RedundantPropertyEvaluator(keyName, JsonSchema.FALSE));
        }
    }

    protected class NegatedPropertiesEvaluator extends AbstractDisjunctivePropertiesEvaluator {

        private final JsonSchema defaultSchema;
        private final List<JsonSchema> subschemas = new ArrayList<>();

        NegatedPropertiesEvaluator(JsonSchema defaultSchema) {
            super(AbstractProperties.this);
            this.defaultSchema = defaultSchema;
        }

        @Override
        public void updateChildren(Event event, JsonParser parser) {
            if (event == Event.KEY_NAME) {
                processKeyName(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                appendEvaluators(type);
            }
        }

        protected void processKeyName(String keyName) {
            findSubschemasFor(keyName, subschemas);
            if (subschemas.isEmpty()) {
                findDefaultSchemaFor(keyName);
            } else {
                subschemas.stream()
                    .filter(s->s == JsonSchema.TRUE || s == JsonSchema.EMPTY)
                    .findAny()
                    .ifPresent(s->{
                        appendRedundantPropertyEvaluator(keyName, s);
                        subschemas.clear();
                    });
            }
        }

        private void findDefaultSchemaFor(String keyName) {
            JsonSchema subschema = this.defaultSchema;
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                appendRedundantPropertyEvaluator(keyName, subschema);
            } else {
                subschemas.add(subschema);
            }
        }

        private void appendEvaluators(InstanceType type) {
            for (JsonSchema subschema : this.subschemas) {
                append(subschema.createNegatedEvaluator(type));
            }
            this.subschemas.clear();
        }

        private void appendRedundantPropertyEvaluator(String keyName, JsonSchema schema) {
            append(new RedundantPropertyEvaluator(keyName, schema));
        }
    }
}
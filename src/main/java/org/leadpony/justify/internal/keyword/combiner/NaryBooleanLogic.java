/*
 * Copyright 2018 the Justify authors.
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * N-ary boolean logic.
 * 
 * @author leadpony
 */
abstract class NaryBooleanLogic implements BooleanLogic {
   
    private final List<JsonSchema> subschemas;
    
    protected NaryBooleanLogic(Collection<JsonSchema> subschemas) {
        this.subschemas = new ArrayList<>(subschemas);
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonProvider jsonProvider) {
        LogicalEvaluator.Builder builder = createEvaluatorBuilder(type);
        this.subschemas.stream()
                .map(s->s.createEvaluator(type))
                .filter(Objects::nonNull)
                .forEach(builder::append);
        Evaluator evaluator = builder.build();
        if (evaluator != null) {
            appender.append(evaluator);
        }
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        this.subschemas.stream()
            .map(JsonSchema::toJson)
            .forEach(arrayBuilder::add);
        builder.add(name(), arrayBuilder);
    }

    protected List<JsonSchema> negateSubschemas() {
        return subschemas.stream().map(JsonSchema::negate).collect(Collectors.toList());
    }

    protected abstract LogicalEvaluator.Builder createEvaluatorBuilder(InstanceType type);
}

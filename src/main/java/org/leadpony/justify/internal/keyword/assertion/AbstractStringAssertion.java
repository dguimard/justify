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

package org.leadpony.justify.internal.keyword.assertion;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Localizable;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.keyword.StringKeyword;

/**
 * Assertion on values of string type.
 * 
 * @author leadpony
 */
abstract class AbstractStringAssertion extends AbstractAssertion implements StringKeyword, Evaluator {
  
    private static final Localizable LOCALIZED_KEY = (locale)->Message.asString("string.key", locale);
    private static final Localizable LOCALIZED_VALUE = (locale)->Message.asString("string.value", locale);
    
    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this::evaluateNegated;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        assert event == Event.VALUE_STRING || event == Event.KEY_NAME;
        return evaluateAgainst(parser.getString(), event, parser, dispatcher);
    }
 
    public ProblemBuilder createProblemBuilder(JsonParser parser, Event event) {
        ProblemBuilder builder = super.createProblemBuilder(parser);
        if (event == Event.KEY_NAME) {
            builder.withParameter("subject", "key")
                   .withParameter("localizedSubject", LOCALIZED_KEY); 
        } else {
            builder.withParameter("subject", "value")
                   .withParameter("localizedSubject", LOCALIZED_VALUE); 
        }
        return builder;
    }
    
    private Result evaluateNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        assert event == Event.VALUE_STRING || event == Event.KEY_NAME;
        return evaluateNegatedAgainst(parser.getString(), event, parser, dispatcher);
    }

    /**
     * Evaluates this assertion on a string value.
     * 
     * @param value the value to apply this assertion.
     * @param event the event which produced the string value.
     * @param parser the JSON parser.
     * @param dispatcher the dispatcher by which detected problems will be dispatched.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateAgainst(String value, Event event, JsonParser parser, ProblemDispatcher dispatcher);

    /**
     * Evaluates the negated assertion on a string value.
     * 
     * @param value the value to apply this assertion.
     * @param event the event which produced the string value.
     * @param parser the JSON parser.
     * @param dispatcher the dispatcher by which detected problems will be dispatched.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateNegatedAgainst(String value, Event event, JsonParser parser, ProblemDispatcher dispatcher);
}

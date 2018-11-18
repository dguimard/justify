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

package org.leadpony.justify.core;

import javax.json.stream.JsonParser;

/**
 * Dispatcher of problem found by a JSON validator.
 * 
 * @author leadpony
 */
public interface ProblemDispatcher {
    
    /**
     * Dispatches the problem found.
     * @param problem the problem to dispatch, cannot be {@code null}.
     */
    void dispatchProblem(Problem problem);
    
    /**
     * Dispatches an inevitable problem.
     * @param parser the parser which encountered the problem, cannot be {@code null}.
     * @param schema the JSON schema evaluated, cannot be {@code null}.
     */
    void dispatchInevitableProblem(JsonParser parser, JsonSchema schema);
}
/*
 * Copyright 2019-2021 M. Sean Gilligan.
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
package app.supernaut;

/**
 *  An abstraction of a User Interface application that is separated
 *  into a foreground app and a background app. Currently, it is <b>unused for JavaFX</b>.
 */
public interface ForegroundApp {
    /**
     * Initialize the application
     *
     * @throws Exception an exception occurred
     */
    void init() throws Exception;

    /**
     * Start the application
     *
     * @throws Exception an exception occurred
     */
    default void start() throws Exception {}
    
    /**
     * Stop the application
     *
     * @throws Exception an exception occurred
     */
    void stop() throws Exception;
}

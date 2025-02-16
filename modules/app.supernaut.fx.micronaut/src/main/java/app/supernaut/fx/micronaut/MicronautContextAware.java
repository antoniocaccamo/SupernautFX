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
package app.supernaut.fx.micronaut;

import io.micronaut.context.BeanContext;
import app.supernaut.BackgroundApp;

/**
 *  TODO: Use this somewhere to inject the context
 */
public interface MicronautContextAware extends BackgroundApp {
    /**
     * Implement this method to have the BeanContext injected
     * @param context injected BeanContext
     */
    void setBeanFactory(BeanContext context);
}

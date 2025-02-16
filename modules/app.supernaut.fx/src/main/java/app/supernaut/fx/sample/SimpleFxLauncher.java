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
package app.supernaut.fx.sample;

import app.supernaut.BackgroundApp;
import app.supernaut.fx.ApplicationDelegate;
import app.supernaut.fx.FxLauncherAbstract;

/**
 * A simple {@link FxLauncherAbstract} that uses {@link Class} objects to specify {@link BackgroundApp}
 * and {@link ApplicationDelegate}.
 */
public final class SimpleFxLauncher extends FxLauncherAbstract {

    /**
     * Default constructor using background start
     */
    public SimpleFxLauncher() {
        this(true);
    }

    /**
     * Constructor that lets you choose background start or not
     * @param backgroundStart  true to start on background
     */
    public SimpleFxLauncher(boolean backgroundStart) {
        super(DefaultAppFactory::new, backgroundStart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "simple";
    }
}

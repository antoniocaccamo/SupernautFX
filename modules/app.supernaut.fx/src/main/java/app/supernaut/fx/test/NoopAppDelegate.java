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
package app.supernaut.fx.test;

import app.supernaut.fx.ApplicationDelegate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op ApplicationDelegate which exits immediately (used for testing)
 */
public final class NoopAppDelegate implements ApplicationDelegate {
    private static final Logger log = LoggerFactory.getLogger(NoopAppDelegate.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("Entered");
        primaryStage.show();
        noop();
    }
    
    private void noop() {
        log.info("Calling Plaform.exit()");
        Platform.exit();    // Exit OpenJFX
    }
}

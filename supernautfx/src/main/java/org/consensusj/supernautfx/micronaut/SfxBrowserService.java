/*
 * Copyright 2019-2020 M. Sean Gilligan.
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
package org.consensusj.supernautfx.micronaut;

import io.micronaut.context.annotation.Context;
import javafx.application.HostServices;
import org.consensusj.supernaut.services.BrowserService;

import javax.inject.Singleton;

/**
 * Default implementation of BrowserService using JavaFX HostServices.
 */
@Singleton
public class SfxBrowserService implements BrowserService {
    private final HostServices hostServices;

    /**
     * Constructor
     * @param hostServices HostServices object to wrap
     */
    public SfxBrowserService(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Implementation of showDocument using HostServices
     * @param uri the URI of the web page that will be opened in a browser.
     */
    @Override
    public void showDocument(String uri) {
        hostServices.showDocument(uri);
    }
}

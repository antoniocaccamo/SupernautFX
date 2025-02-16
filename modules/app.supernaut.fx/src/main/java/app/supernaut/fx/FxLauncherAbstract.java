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
package app.supernaut.fx;

import app.supernaut.fx.test.NoopBackgroundApp;
import javafx.application.Application;
import app.supernaut.BackgroundApp;
import app.supernaut.fx.internal.OpenJfxProxyApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * Base JavaFX implementation of {@link FxLauncher}. This implementation provides the following functionality:
 * <ol>
 *     <li>
 *      Starts OpenJFX applications.
 *     </li>
 *     <li>
 *      Constructor provides an option to start {@link BackgroundApp} on a new thread (this allows
 *      the {@code BackgroundApp} and the OpenJFX {@link ApplicationDelegate} to initialize in parallel.)
 *     </li>
 *     <li>
 *      Implements {@link FxLauncher#launchAsync} which initializes the OpenJFX {@link ApplicationDelegate} on
 *      a new thread. This is not needed for a typical, packaged OpenJFX application
 *      which can just call {@link FxLauncher#launch} from its {@code static main()}, but is useful
 *      in various testing scenarios.
 *     </li>
 *     <li>
 *      Defines the {@link AppFactory} interface for constructing the {@link BackgroundApp} and {@link ApplicationDelegate}.
 *      This allows subclasses (or callers) to provide their own implementation of the application creation logic. The
 *      AppFactory interface was designed to allow usage of Dependency Injection frameworks like <b>Micronaut</b>
 *      to create dependency-injected implementations of {@link ApplicationDelegate} and {@link BackgroundApp}. The {@link AppFactory AppFactory}
 *      interface was also designed to be lazily-instantiated so the {@link AppFactory AppFactory} (dependency-injection framework)
 *      can initialize in parallel to OpenJFX.
 *     </li>
 *     <li>
 *      Uses the same {@link AppFactory AppFactory} (dependency-injection context) to initialize the ApplicationDelegate and Background
 *      application. A {@code CountDownLatch} is used to make sure the {@link AppFactory AppFactory} (which may be initialized
 *      on another thread along with the BackgroundApp) is ready when {@link OpenJfxProxyApplication} calls
 *      {@code createAppDelegate(Application proxyApplication)}.
 *     </li>
 * </ol>
 *
 */
public abstract class FxLauncherAbstract implements FxLauncher {
    private static final Logger log = LoggerFactory.getLogger(FxLauncherAbstract.class);
    private static final String backgroundAppLauncherThreadName = "SupernautFX-Background-Launcher";
    private static final String foregroundAppLauncherThreadName = "SupernautFX-JavaFX-Launcher";

    private final boolean initializeBackgroundAppOnNewThread;
    private final Supplier<AppFactory> appFactorySupplier;
    private final CountDownLatch appFactoryInitializedLatch;
    private AppFactory appFactory;

    /** This future returns an initialized BackgroundApp */
    protected final CompletableFuture<BackgroundApp> futureBackgroundApp = new CompletableFuture<>();
    /** This future returns an initialized ApplicationDelegate */
    protected final CompletableFuture<ApplicationDelegate> futureAppDelegate = new CompletableFuture<>();

    /* Temporary storage of appDelegateClass for interaction with OpenJfxProxyApplication */
    private Class<? extends ApplicationDelegate> appDelegateClass;

    /**
     * Interface that can be used to create and pre-initialize {@link ApplicationDelegate} and {@link BackgroundApp}.
     * This interface can be implemented by subclasses (or direct callers of the constructor.) By "pre-initialize" we
     * mean call implementation-dependent methods prior to {@code init()} or {@code start()}.
     * This interface is designed to support using Dependency Injection frameworks like Micronaut, see
     * {@code MicronautSfxLauncher}.
     */
    public interface AppFactory {
        /**
         * Create the background class instance from a {@link Class} object
         * @param backgroundAppClass the class to create
         * @return application instance
         */
        BackgroundApp   createBackgroundApp(Class<? extends BackgroundApp> backgroundAppClass);

        /**
         * Create the background class instance from a {@link Class} object
         * @param appDelegateClass  the class to create
         * @param proxyApplication a reference to the proxy {@link Application} created by Supernaut.FX
         * @return application instance
         */
        ApplicationDelegate createAppDelegate(Class<? extends ApplicationDelegate> appDelegateClass, Application proxyApplication);
    }

    /**
     * Default implementation of AppFactory.
     */
    public static class DefaultAppFactory implements AppFactory {
        
        @Override
        public BackgroundApp createBackgroundApp(Class<? extends BackgroundApp> backgroundAppClass) {
            return newInstance(backgroundAppClass);
        }

        @Override
        public ApplicationDelegate createAppDelegate(Class<? extends ApplicationDelegate> appDelegateClass, Application proxyApplication) {
            return newInstance(appDelegateClass);
        }

        /**
         * newInstance without checked exceptions.
         *
         * @param clazz A Class object that must have a no-args constructor.
         * @param <T> The type of the class
         * @return A new instanceof the class
         * @throws RuntimeException exceptions thrown by {@code newInstance()}.
         */
        private static <T> T newInstance(Class<T> clazz) {
            T appDelegate;
            try {
                appDelegate = clazz.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return appDelegate;
        }
    }

    /**
     * Construct an Asynchronous Launcher that works with OpenJFX.
     * 
     * @param appFactorySupplier A Supplier that will lazily instantiate an AppFactory.
     * @param initializeBackgroundAppOnNewThread If true, initializes {@code appFactorySupplier} and
     *        {@code BackgroundApp} on new thread, if false start them on calling thread (typically the main thread)
     */
    public FxLauncherAbstract(Supplier<AppFactory> appFactorySupplier, boolean initializeBackgroundAppOnNewThread) {
        this.appFactorySupplier = appFactorySupplier;
        this.initializeBackgroundAppOnNewThread = initializeBackgroundAppOnNewThread;
        appFactoryInitializedLatch = new CountDownLatch(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ApplicationDelegate> launchAsync(String[] args, Class<? extends ApplicationDelegate> appDelegate, Class<? extends BackgroundApp> backgroundApp) {
        log.info("launchAsync...");
        launchInternal(args, appDelegate, backgroundApp, true);
        return getAppDelegate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(String[] args, Class<? extends ApplicationDelegate> appDelegate, Class<? extends BackgroundApp> backgroundApp) {
        log.info("launch...");
        launchInternal(args, appDelegate, backgroundApp, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(String[] args, Class<? extends ApplicationDelegate> appDelegate) {
        launch(args, appDelegate, NoopBackgroundApp.class);
    }

    /**
     * Called by {@code OpenJfxProxyApplication} to create its delegate {@link ApplicationDelegate} object.
     * Waits on a {@link CountDownLatch} to make sure the {@link AppFactory AppFactory} is ready.
     * 
     * @param proxyApplication The calling instance of {@code OpenJfxProxyApplication}
     * @return The newly constructed OpenJFX-compatible {@link ApplicationDelegate}
     */
    @Override
    public ApplicationDelegate createAppDelegate(Application proxyApplication) {
        try {
            appFactoryInitializedLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ApplicationDelegate appDelegate = appFactory.createAppDelegate(appDelegateClass, proxyApplication);
        appDelegate.setApplication(proxyApplication);
        // TODO: Create a LauncherAware interface for injecting the launcher into apps?
        futureAppDelegate.complete(appDelegate);
        return appDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<ApplicationDelegate> getAppDelegate() {
        return futureAppDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<BackgroundApp> getBackgroundApp() {
        return futureBackgroundApp;
    }

    /**
     * Internal launch method called by both {@code launchAsync()} and {@code launch()}
     * @param args Command-line arguments to pass to the OpenJFX application
     * @param initForegroundOnNewThread If true, start OpenJFX on a new thread, if false start it on
     *                        calling thead (typically this will be the main thread)
     */
    private void launchInternal(String[] args, Class<? extends ApplicationDelegate> appDelegateClass, Class<? extends BackgroundApp> backgroundAppClass, boolean initForegroundOnNewThread) {
        launchBackgroundApp(backgroundAppClass);
        launchForegroundApp(args, appDelegateClass, initForegroundOnNewThread);
    }

    private void launchBackgroundApp(Class<? extends BackgroundApp> backgroundAppClass) {
        if (initializeBackgroundAppOnNewThread) {
            log.info("Launching background app on {} thread", backgroundAppLauncherThreadName);
            startThread(backgroundAppLauncherThreadName,  () -> startBackgroundApp(backgroundAppClass));
        } else {
            log.info("Launching background app on caller's thread");
            startBackgroundApp(backgroundAppClass);
        }
    }

    private void launchForegroundApp(String[] args, Class<? extends ApplicationDelegate> appDelegateClass, boolean async) {
        if (async) {
            log.info("Launching on {} thread", foregroundAppLauncherThreadName);
            startThread(foregroundAppLauncherThreadName, () -> startForegroundApp(args, appDelegateClass));
        } else {
            log.info("Launching on caller's thread");
            startForegroundApp(args, appDelegateClass);
        }
    }

    private void startBackgroundApp(Class<? extends BackgroundApp> backgroundAppClass) {
        log.info("Instantiating appFactory");
        this.appFactory = appFactorySupplier.get();

        /*
         * Tell the foreground app thread that appFactory is initialized.
         */
        log.info("Release appFactoryInitializedLatch");
        appFactoryInitializedLatch.countDown();

        log.info("Instantiating backgroundApp class");
        BackgroundApp backgroundApp = appFactory.createBackgroundApp(backgroundAppClass);

        /*
         * Do any (hopefully minimal) background initialization that
         * is needed before starting the foreground
         */
        log.info("Init backgroundApp");
        backgroundApp.init();

        futureBackgroundApp.complete(backgroundApp);


        /*
         * Call the background app, so it can start its own threads
         */
        backgroundApp.start();
    }
    
    private void startForegroundApp(String[] args, Class<? extends ApplicationDelegate> appDelegateClass) {
        OpenJfxProxyApplication.configuredLauncher = this;
        this.appDelegateClass = appDelegateClass;
        log.info("Calling Application.launch()");
        Application.launch(OpenJfxProxyApplication.class, args);
        log.info("OpenJfxProxyApplication exited.");
    }

    private Thread startThread(String threadName, Runnable target) {
        Thread thread = new Thread(target);
        thread.setName(threadName);
        thread.start();
        return thread;
    }
}

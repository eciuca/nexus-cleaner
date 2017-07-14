package com.github.eciuca.tools.nexuscleaner.modules;

import be.fluid_it.bootique.vertx.VertxModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.github.eciuca.tools.nexuscleaner.config.NexusCleanerConfiguration;
import com.github.eciuca.tools.nexuscleaner.verticles.ComputeVersionsToDeleteVerticle;
import com.github.eciuca.tools.nexuscleaner.verticles.EntryVerticle;
import com.github.eciuca.tools.nexuscleaner.verticles.GetArtifactVersionsFromNexus;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.rxjava.ext.web.handler.sockjs.BridgeEvent;

import javax.annotation.Nullable;

public class NexusCleanerModule extends ConfigModule {

    public static final String NEXUS_CLEANER_CONFIG_IDENTIFIER = "nexusCleaner";

    @Override
    public void configure(Binder binder) {
        VertxModule.extend(binder)
                .addVerticle(EntryVerticle.class)
                .addVerticle(ComputeVersionsToDeleteVerticle.class)
                .addVerticle(GetArtifactVersionsFromNexus.class);
    }

    @Provides @Singleton
    public HttpClientOptions createHttpClientOptions() {
        HttpClientOptions options = new HttpClientOptions();
        options.setLogActivity(true);

        return options;
    }

    @Provides @Singleton
    private NexusCleanerConfiguration providesNexusCleanerConfiguration(ConfigurationFactory configFactory) {
        return configFactory.config(NexusCleanerConfiguration.class, NEXUS_CLEANER_CONFIG_IDENTIFIER);
    }

    @Nullable
    @Provides
    private Handler<BridgeEvent> provideBridgeEventHandler() {
        return null;
    }
}

package com.github.eciuca.tools.nexuscleaner.app;

import com.github.eciuca.tools.nexuscleaner.modules.NexusCleanerModule;
import io.bootique.Bootique;

public class NexusCleanerService {
    public static void main(String[] args) {
        Bootique
                .app("--engine", "--config=classpath:nexus-cleaner.yml")
                .autoLoadModules()
                .module(NexusCleanerModule.class)
                .run();
    }
}

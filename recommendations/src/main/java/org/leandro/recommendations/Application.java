package org.leandro.recommendations;

import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.scheduling.TaskScheduler;
import io.micronaut.scheduling.annotation.Async;
import org.leandro.recommendations.feature.RecommendationRepository;

import javax.inject.Singleton;


@Singleton
public class Application {


    private final TaskScheduler taskScheduler;
    private final RecommendationRepository recommendationsRepository;

    public Application(TaskScheduler taskScheduler, RecommendationRepository recommendationsRepository) {
        this.taskScheduler = taskScheduler;
        this.recommendationsRepository = recommendationsRepository;
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @EventListener
    @Async
    public void onStartup(final ServerStartupEvent event) {
        recommendationsRepository.createInitialRecommendations();
    }
}

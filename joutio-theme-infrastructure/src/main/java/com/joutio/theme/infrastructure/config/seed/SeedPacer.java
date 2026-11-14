package com.joutio.theme.infrastructure.config.seed;

import com.joutio.theme.infrastructure.properties.AppProperties;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;

/**
 * Cadence adaptative du seed : après chaque question, une pause proportionnelle au temps passé à
 * envoyer ses commandes (plafonnée), une pause entre thèmes traités et un délai de démarrage. Plus
 * le cluster est chargé, plus les commandes sont lentes et plus la pause s'allonge : la charge est
 * étalée dans la durée au lieu d'un pic.
 */
public final class SeedPacer {

    /** Attente bloquante, injectable pour les tests. */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final AppProperties.Pacing pacing;
    private final Sleeper sleeper;
    private final Counter pauseSeconds;

    public SeedPacer(AppProperties.Pacing pacing, MeterRegistry meterRegistry, Sleeper sleeper) {
        this.pacing = pacing == null ? AppProperties.Pacing.NONE : pacing;
        this.sleeper = sleeper;
        this.pauseSeconds =
                Counter.builder("joutio.theme.seed.pause.seconds")
                        .description("Temps de pause volontaire du seeder (cadence)")
                        .register(meterRegistry);
    }

    public SeedPacer(AppProperties.Pacing pacing, MeterRegistry meterRegistry) {
        this(pacing, meterRegistry, duration -> Thread.sleep(duration.toMillis()));
    }

    /** Pause à observer après une question dont les commandes ont pris {@code spent}. */
    public Duration questionPause(Duration spent) {
        if (pacing.dutyRatio() <= 0 || spent.isNegative() || spent.isZero()) {
            return Duration.ZERO;
        }
        Duration pause = Duration.ofMillis(Math.round(spent.toMillis() * pacing.dutyRatio()));
        Duration max = pacing.maxQuestionPause();
        return max != null && pause.compareTo(max) > 0 ? max : pause;
    }

    public void beforeStart() {
        pause(pacing.startDelay());
    }

    public void afterQuestion(Duration spent) {
        pause(questionPause(spent));
    }

    public void afterTopic() {
        pause(pacing.topicPause());
    }

    private void pause(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return;
        }
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while pacing the seed", e);
        }
        pauseSeconds.increment(duration.toMillis() / 1000.0);
    }
}

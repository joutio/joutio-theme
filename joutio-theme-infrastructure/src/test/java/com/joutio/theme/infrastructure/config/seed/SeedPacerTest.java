package com.joutio.theme.infrastructure.config.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.joutio.theme.infrastructure.properties.AppProperties;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

class SeedPacerTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final List<Duration> sleeps = new ArrayList<>();

    private SeedPacer pacer(AppProperties.Pacing pacing) {
        return new SeedPacer(pacing, meterRegistry, sleeps::add);
    }

    @Test
    void neutralPacingNeverSleeps() {
        SeedPacer pacer = pacer(AppProperties.Pacing.NONE);

        pacer.beforeStart();
        pacer.afterQuestion(Duration.ofMillis(300));
        pacer.afterTopic();

        assertThat(sleeps).isEmpty();
    }

    @Test
    void questionPauseIsProportionalToTimeSpent() {
        SeedPacer pacer =
                pacer(new AppProperties.Pacing(Duration.ZERO, 1.5, Duration.ofSeconds(2), Duration.ZERO));

        assertThat(pacer.questionPause(Duration.ofMillis(200))).isEqualTo(Duration.ofMillis(300));
        assertThat(pacer.questionPause(Duration.ZERO)).isEqualTo(Duration.ZERO);
    }

    @Test
    void questionPauseIsCapped() {
        SeedPacer pacer =
                pacer(new AppProperties.Pacing(Duration.ZERO, 1.0, Duration.ofSeconds(2), Duration.ZERO));

        assertThat(pacer.questionPause(Duration.ofSeconds(5))).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void startDelayAndTopicPauseAreObservedAndMeasured() {
        SeedPacer pacer =
                pacer(
                        new AppProperties.Pacing(
                                Duration.ofSeconds(60), 1.0, Duration.ofSeconds(2), Duration.ofSeconds(5)));

        pacer.beforeStart();
        pacer.afterQuestion(Duration.ofMillis(100));
        pacer.afterTopic();

        assertThat(sleeps)
                .containsExactly(Duration.ofSeconds(60), Duration.ofMillis(100), Duration.ofSeconds(5));
        assertThat(meterRegistry.get("joutio.theme.seed.pause.seconds").counter().count())
                .isEqualTo(65.1, org.assertj.core.data.Offset.offset(0.001));
    }
}

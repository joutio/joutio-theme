package com.joutio.theme.infrastructure.config.seed;

import com.joutio.theme.domain.model.TopicTitles;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou de contenu : les titres personnalisés du seed réel ({@code topic.titles}) sont
 * **optionnels**, mais lorsqu'ils sont présents ils sont bien formés : paliers connus, libellés
 * non vides et bornés (le repli « palier · nom du sujet » couvre les manques).
 */
class SeedTopicTitlesTest {

    @Test
    void seedTitlesAreWellFormedWhenPresent() {
        SeedDataLoader loader = new SeedDataLoader(
                "classpath*:seed/topics/*/*.yml", "https://media.example.test/joutio-media/",
                new PathMatchingResourcePatternResolver());

        List<TopicSeedDefinition> topics = loader.loadAll();
        assertThat(topics).hasSizeGreaterThanOrEqualTo(20);

        for (TopicSeedDefinition topic : topics) {
            if (topic.titles() == null) {
                continue;
            }
            topic.titles().forEach((language, byTier) -> {
                assertThat(byTier)
                        .as("titres vides en %s — %s", language, topic.topicId())
                        .isNotEmpty();
                byTier.forEach((tier, label) -> {
                    assertThat(TopicTitles.TIERS)
                            .as("palier inconnu '%s' — %s", tier, topic.topicId())
                            .contains(tier);
                    assertThat(label)
                            .as("libellé vide (%s) — %s", tier, topic.topicId())
                            .isNotBlank();
                    assertThat(label.length())
                            .as("libellé trop long (%s) — %s", tier, topic.topicId())
                            .isLessThanOrEqualTo(TopicTitles.MAX_LABEL_LENGTH);
                });
            });
        }
    }
}

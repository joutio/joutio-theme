package com.joutio.theme.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Propriétés applicatives de {@code joutio-theme} ({@code app.*}) en record immuable.
 */
@ConfigurationProperties("app")
public record AppProperties(
        @DefaultValue SeedData seedData) {

    public record SeedData(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("classpath*:seed/topics/*/*.yml") String location,
            /**
             * Base publique des images du seed : les {@code imageUrl} relatifs des YAML
             * (ex. {@code wikipedia/<categorie>/<fichier>}) sont résolus en {@code base + chemin}.
             * Les URLs absolues http(s) restent acceptées telles quelles.
             */
            @DefaultValue("") String imageBaseUrl,
            @DefaultValue Pacing pacing) {
    }

    /**
     * Cadence du seed ({@code app.seed-data.pacing.*}) : lisse la charge d'un seed complet (≈ 32 000
     * commandes après un reset) au lieu d'une rafale. Valeurs par défaut neutres (aucune pause) ;
     * la prod les active ({@code application-prod.yml}).
     *
     * @param startDelay attente avant le premier thème, après le démarrage de l'application
     * @param dutyRatio pause après chaque question = durée de ses commandes × ratio (1.0 ≈ 50 % de
     *     cycle utile) : la pause s'allonge d'elle-même quand le cluster ralentit
     * @param maxQuestionPause plafond de la pause par question
     * @param topicPause pause après un thème créé ou réparé (pas après un thème inchangé)
     */
    public record Pacing(
            @DefaultValue("0s") Duration startDelay,
            @DefaultValue("0") double dutyRatio,
            @DefaultValue("2s") Duration maxQuestionPause,
            @DefaultValue("0s") Duration topicPause) {

        public static final Pacing NONE = new Pacing(Duration.ZERO, 0, Duration.ZERO, Duration.ZERO);
    }
}

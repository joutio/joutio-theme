package com.joutio.theme.infrastructure.config.seed;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.Question;

/**
 * Identité stable d'une question de seed : le texte peut être dupliqué dans un thème
 * (questions visuelles, ex. « Quel est ce pays ? »), la paire (texte, image) est en
 * revanche unique par fichier. Sert de clé de réparation à {@code DataSeeder}.
 * <p>Le texte de référence est le contenu français (toujours présent dans le seed).</p>
 * <p>L'image est identifiée par son chemin relatif dans le seed ({@code wikipedia/<categorie>/<fichier>}),
 * jamais par l'URL complète : un changement de {@code app.seed-data.image-base-url} ne doit pas faire
 * recréer la question (doublon approuvé dans le tirage, 2026-11-16).</p>
 */
public record QuestionIdentity(String text, String imageKey) {

    /** Segments de chemin gardés : {@code wikipedia/<categorie>/<fichier>}. */
    private static final int IMAGE_KEY_SEGMENTS = 3;

    public static QuestionIdentity of(QuestionSeedDefinition definition) {
        return new QuestionIdentity(definition.contents().get(Language.FR).text(), imageKey(definition.imageUrl()));
    }

    public static QuestionIdentity of(Question question) {
        return new QuestionIdentity(question.text(), imageKey(question.imageUrl()));
    }

    /** Fin du chemin de l'image (au plus trois segments), sans schéma, hôte ni préfixe de bucket. */
    static String imageKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String path = imageUrl.trim();
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        String[] segments = path.split("/");
        int from = Math.max(0, segments.length - IMAGE_KEY_SEGMENTS);
        return String.join("/", java.util.Arrays.copyOfRange(segments, from, segments.length));
    }
}

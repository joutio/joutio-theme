package com.joutio.theme.domain.model;

import com.joutio.microservice.core.domain.model.i18n.Language;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Titres **thématiques personnalisés** d'un sujet : un libellé par **palier de rareté** et par
 * langue (ex. Histoire → {@code OR: "Maréchal d'Empire"}). Optionnels (le client retombe alors
 * sur « palier · nom du sujet »).
 *
 * <p>Les clés de palier sont le **miroir** de `MasteryTier` (joutio-profile) — le domaine theme
 * ne dépend jamais d'un autre `*-domain` — et sont validées ici : toute clé inconnue est rejetée.</p>
 */
public final class TopicTitles {

    /** Clés de palier attendues (miroir de `MasteryTier`), dans l'ordre croissant. */
    public static final List<String> TIERS = List.of(
            "FER", "BRONZE", "ARGENT", "OR", "PLATINE", "EMERAUDE", "DIAMANT");

    public static final int MAX_LABEL_LENGTH = 40;

    private TopicTitles() {
    }

    /**
     * Normalise et valide la carte des titres : langues et clés de palier inconnues rejetées,
     * libellés vides ou trop longs rejetés, entrées vides supprimées. Renvoie {@code null} si
     * rien n'est défini (titre absent = repli client).
     */
    public static Map<Language, Map<String, String>> normalize(
            Map<Language, Map<String, String>> titles) {
        if (titles == null || titles.isEmpty()) {
            return null;
        }
        Map<Language, Map<String, String>> clean = new LinkedHashMap<>();
        titles.forEach((language, byTier) -> {
            if (language == null) {
                throw new IllegalArgumentException("title language is required");
            }
            if (byTier == null || byTier.isEmpty()) {
                return;
            }
            Map<String, String> labels = new LinkedHashMap<>();
            byTier.forEach((tier, label) -> {
                String key = requireTier(tier);
                if (label == null || label.isBlank()) {
                    throw new IllegalArgumentException("title label is required for " + key);
                }
                String value = label.trim();
                if (value.length() > MAX_LABEL_LENGTH) {
                    throw new IllegalArgumentException(
                            "title label too long for " + key + " (" + value.length() + " > "
                                    + MAX_LABEL_LENGTH + ")");
                }
                labels.put(key, value);
            });
            if (!labels.isEmpty()) {
                clean.put(language, labels);
            }
        });
        return clean.isEmpty() ? null : clean;
    }

    private static String requireTier(String tier) {
        String key = tier == null ? "" : tier.trim().toUpperCase();
        if (!TIERS.contains(key)) {
            throw new IllegalArgumentException("unknown mastery tier '" + tier + "'");
        }
        return key;
    }
}

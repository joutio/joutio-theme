package com.joutio.theme.domain.model;

/**
 * Univers d'un sujet, alignés sur les 17 catégories du design (`design/tokens`, libellés FR identiques).
 *
 * <p>{@code RELIGION} a été retirée le 2026-11-12 : ses sujets relèvent de la culture générale. La prod repart d'une
 * base vierge (script de reset) ; la migration V5 convertit les bases existantes.
 */
public enum TopicCategory {
    ARTS("Arts"),
    BUSINESS("Business"),
    EDUCATION("Éducation"),
    ENTERTAINMENT("Divertissement"),
    FOOD_AND_DRINK("Gastronomie"),
    GAMES("Jeux"),
    GENERAL("Culture générale"),
    HISTORY("Histoire"),
    LITERATURE("Littérature"),
    MOVIES("Cinéma"),
    MUSIC("Musique"),
    NATURE("Nature"),
    SCIENCE("Science"),
    SPORTS("Sport"),
    TELEVISION("Télévision"),
    TECHNOLOGY("Technologie"),
    WORLD("Monde");

    private final String label;

    TopicCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

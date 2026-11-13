package com.joutio.theme.domain.command;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.TopicCategory;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Map;

public interface TopicCommand {
    String topicId();

    /**
     * Commande pour créer un nouveau thème de quiz. {@code names} porte un nom par langue (FR
     * obligatoire, EN optionnel).
     */
    record CreateTopicCommand(
            @TargetAggregateIdentifier String topicId,
            Map<Language, String> names,
            String description,
            TopicCategory category,
            String imageUrl,
            /** Pictogramme carré (optionnel). */
            String pictogramUrl,
            /** Titres personnalisés par palier et par langue (optionnels). */
            Map<Language, Map<String, String>> titles,
            String creatorId)
            implements TopicCommand {}

    /**
     * Mise à jour des titres personnalisés (propriétaire du thème uniquement) : une carte de
     * libellés par palier et par langue, ou {@code null} pour revenir au repli client.
     */
    record UpdateTopicTitlesCommand(
            @TargetAggregateIdentifier String topicId,
            String requestedBy,
            Map<Language, Map<String, String>> titles)
            implements TopicCommand {}

    /**
     * Commande pour publier un thème (transition DRAFT -> PUBLISHED) Requiert au minimum 7
     * questions approuvées
     */
    record PublishTopicCommand(@TargetAggregateIdentifier String topicId, String requesterId)
            implements TopicCommand {}

    /**
     * Repasse un sujet publié en brouillon (suppression d'un compte ayant retiré des questions).
     */
    record DraftTopicCommand(@TargetAggregateIdentifier String topicId, String reason)
            implements TopicCommand {}

    /** Mise à jour du nom d'une langue (propriétaire du thème uniquement). */
    record UpdateTopicNameCommand(
            @TargetAggregateIdentifier String topicId,
            String requestedBy,
            Language language,
            String name)
            implements TopicCommand {}

    /** Mise à jour de la description (propriétaire du thème uniquement). */
    record UpdateTopicDescriptionCommand(
            @TargetAggregateIdentifier String topicId,
            String requestedBy,
            String description,
            /** Langue visée ({@code null} : français, comme avant le 2026-11-13). */
            Language language)
            implements TopicCommand {}

    /** Mise à jour de la catégorie (propriétaire du thème uniquement). */
    record UpdateTopicCategoryCommand(
            @TargetAggregateIdentifier String topicId, String requestedBy, TopicCategory category)
            implements TopicCommand {}

    /** Mise à jour de l'illustration de couverture (propriétaire du thème uniquement). */
    record UpdateTopicImageUrlCommand(
            @TargetAggregateIdentifier String topicId, String requestedBy, String imageUrl)
            implements TopicCommand {}

    /**
     * Mise à jour de la bannière de la fiche sujet, distincte de la couverture (propriétaire du
     * thème uniquement).
     */
    record UpdateTopicBannerUrlCommand(
            @TargetAggregateIdentifier String topicId, String requestedBy, String bannerUrl)
            implements TopicCommand {}

    /**
     * Mise à jour du pictogramme carré de la tuile du catalogue, ou {@code null} pour l'effacer
     * (propriétaire du thème uniquement).
     */
    record UpdateTopicPictogramUrlCommand(
            @TargetAggregateIdentifier String topicId, String requestedBy, String pictogramUrl)
            implements TopicCommand {}

    record SetTopicMemberCommand(
            @TargetAggregateIdentifier String topicId,
            String requestedBy,
            String userId,
            com.joutio.theme.domain.model.TopicTeamRole role)
            implements TopicCommand {}

    record RemoveTopicMemberCommand(
            @TargetAggregateIdentifier String topicId, String requestedBy, String userId)
            implements TopicCommand {}

    record UnpublishTopicCommand(@TargetAggregateIdentifier String topicId, String requestedBy)
            implements TopicCommand {}

    /** Supprime un brouillon : réservé au créateur, refusé sur un sujet publié. */
    record DeleteTopicCommand(@TargetAggregateIdentifier String topicId, String requestedBy)
            implements TopicCommand {}

    record RemoveDeletedTopicMemberCommand(@TargetAggregateIdentifier String topicId, String userId)
            implements TopicCommand {}
}

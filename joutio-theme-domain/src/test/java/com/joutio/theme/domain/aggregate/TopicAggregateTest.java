package com.joutio.theme.domain.aggregate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.joutio.axon.test.JoutioAxonMatchers;
import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.event.TopicEvent;
import com.joutio.theme.domain.exception.QuestionProblems;
import com.joutio.theme.domain.exception.TopicProblems;
import com.joutio.theme.domain.model.TopicCategory;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

/**
 * Test Axon in-memory de l'agrégat {@link TopicAggregate} : création, publication et mises à jour
 * champ par champ (propriétaire uniquement).
 */
class TopicAggregateTest {

    private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");
    private static final String TOPIC_ID = "t-1";
    private static final String OWNER = "creator-1";

    private final AggregateTestFixture<TopicAggregate> fixture =
            new AggregateTestFixture<>(TopicAggregate.class);
    private final QuestionRepositoryPort questionRepository = mock(QuestionRepositoryPort.class);

    @BeforeEach
    void setUp() {
        fixture.registerInjectableResource(questionRepository);
    }

    @Test
    void createTopic_appliesTopicCreatedEvent() {
        fixture.givenNoPriorActivity()
                .when(createCommand("Cinéma", "Tout le cinéma"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicCreatedEvent.class,
                                e ->
                                        TOPIC_ID.equals(
                                                        ((TopicEvent.TopicCreatedEvent) e)
                                                                .topicId())
                                                && OWNER.equals(
                                                        ((TopicEvent.TopicCreatedEvent) e)
                                                                .creatorId())
                                                && TopicCategory.MOVIES
                                                        == ((TopicEvent.TopicCreatedEvent) e)
                                                                .category()));
    }

    @Test
    void createTopic_withBlankName_rejects() {
        fixture.givenNoPriorActivity()
                .when(createCommand("   ", null))
                .expectException(TopicProblems.TopicNameEmptyProblem.class);
    }

    @Test
    void createTopic_withNameTooLong_rejects() {
        fixture.givenNoPriorActivity()
                .when(createCommand("a".repeat(256), null))
                .expectException(TopicProblems.TopicNameTooLongProblem.class);
    }

    @Test
    void createTopic_withDescriptionTooLong_rejects() {
        fixture.givenNoPriorActivity()
                .when(createCommand("Cinéma", "a".repeat(501)))
                .expectException(TopicProblems.TopicDescriptionTooLongProblem.class);
    }

    @Test
    void publishTopic_byOwner_appliesTopicPublishedEvent() {
        when(questionRepository.countPlayableByTopicId(TOPIC_ID)).thenReturn(7);

        fixture.given(createdTopic())
                .when(new TopicCommand.PublishTopicCommand(TOPIC_ID, OWNER))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicPublishedEvent.class,
                                e ->
                                        TOPIC_ID.equals(
                                                ((TopicEvent.TopicPublishedEvent) e).topicId())));
    }

    @Test
    void draftTopic_whenPublished_appliesTopicDraftedEvent() {
        fixture.given(createdTopic(), new TopicEvent.TopicPublishedEvent(TOPIC_ID, OWNER, NOW))
                .when(new TopicCommand.DraftTopicCommand(TOPIC_ID, "ACCOUNT_DELETED"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicDraftedEvent.class,
                                e ->
                                        TOPIC_ID.equals(
                                                        ((TopicEvent.TopicDraftedEvent) e)
                                                                .topicId())
                                                && "ACCOUNT_DELETED"
                                                        .equals(
                                                                ((TopicEvent.TopicDraftedEvent) e)
                                                                        .reason())));
    }

    @Test
    void draftTopic_whenAlreadyDraft_emitsNoEvent() {
        fixture.given(createdTopic())
                .when(new TopicCommand.DraftTopicCommand(TOPIC_ID, "ACCOUNT_DELETED"))
                .expectNoEvents();
    }

    @Test
    void publishTopic_byNonOwner_rejects() {
        fixture.given(createdTopic())
                .when(new TopicCommand.PublishTopicCommand(TOPIC_ID, "intruder"))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    @Test
    void publishTopic_withoutEnoughApprovedQuestions_rejects() {
        when(questionRepository.countPlayableByTopicId(TOPIC_ID)).thenReturn(3);

        fixture.given(createdTopic())
                .when(new TopicCommand.PublishTopicCommand(TOPIC_ID, OWNER))
                .expectException(QuestionProblems.NotEnoughApprovedQuestionsProblem.class);
    }

    @Test
    void updateName_byOwner_appliesEvent() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicNameCommand(
                                TOPIC_ID, OWNER, Language.FR, "Séries"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicNameUpdatedEvent.class,
                                e ->
                                        "Séries"
                                                .equals(
                                                        ((TopicEvent.TopicNameUpdatedEvent) e)
                                                                .name())));
    }

    @Test
    void updateName_byNonOwner_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicNameCommand(
                                TOPIC_ID, "intruder", Language.FR, "Séries"))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    @Test
    void updateName_withSameValue_emitsNoEvent() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicNameCommand(
                                TOPIC_ID, OWNER, Language.FR, "Cinéma"))
                .expectNoEvents();
    }

    @Test
    void updateName_withTooLongValue_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicNameCommand(
                                TOPIC_ID, OWNER, Language.FR, "a".repeat(256)))
                .expectException(TopicProblems.TopicNameTooLongProblem.class);
    }

    @Test
    void updateDescription_appliesEvent() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicDescriptionCommand(
                                TOPIC_ID, OWNER, "Nouvelle description", null))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicDescriptionUpdatedEvent.class,
                                e ->
                                        "Nouvelle description"
                                                .equals(
                                                        ((TopicEvent.TopicDescriptionUpdatedEvent)
                                                                        e)
                                                                .description())));
    }

    @Test
    void updateDescription_inEnglish_keepsFrenchAndTagsLanguage() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicDescriptionCommand(
                                TOPIC_ID, OWNER, "English description", Language.EN))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicDescriptionUpdatedEvent.class,
                                e -> {
                                    var event = (TopicEvent.TopicDescriptionUpdatedEvent) e;
                                    return event.language() == Language.EN
                                            && "English description".equals(event.description());
                                }));
    }

    @Test
    void updateDescription_legacyEventWithoutLanguage_isFrench() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicDescriptionUpdatedEvent(
                                TOPIC_ID, OWNER, "Ancienne", java.time.Instant.now(), null))
                .when(
                        new TopicCommand.UpdateTopicDescriptionCommand(
                                TOPIC_ID, OWNER, "Ancienne", Language.FR))
                .expectNoEvents();
    }

    @Test
    void updateCategory_withNull_rejects() {
        fixture.given(createdTopic())
                .when(new TopicCommand.UpdateTopicCategoryCommand(TOPIC_ID, OWNER, null))
                .expectException(TopicProblems.TopicCategoryEmptyProblem.class);
    }

    @Test
    void updateImageUrl_withTooLongValue_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicImageUrlCommand(
                                TOPIC_ID, OWNER, "https://x.test/" + "a".repeat(1100)))
                .expectException(TopicProblems.TopicImageUrlTooLongProblem.class);
    }

    @Test
    void updateBannerUrl_appliesEvent() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicBannerUrlCommand(
                                TOPIC_ID, OWNER, "https://media.test/banner.jpg"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicBannerUrlUpdatedEvent.class,
                                e ->
                                        "https://media.test/banner.jpg"
                                                .equals(
                                                        ((TopicEvent.TopicBannerUrlUpdatedEvent) e)
                                                                .bannerUrl())));
    }

    @Test
    void updateBannerUrl_withSameValue_isNoOp() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicBannerUrlUpdatedEvent(
                                TOPIC_ID, OWNER, "https://media.test/banner.jpg", NOW))
                .when(
                        new TopicCommand.UpdateTopicBannerUrlCommand(
                                TOPIC_ID, OWNER, "https://media.test/banner.jpg"))
                .expectNoEvents();
    }

    @Test
    void updateBannerUrl_withNull_clearsBanner() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicBannerUrlUpdatedEvent(
                                TOPIC_ID, OWNER, "https://media.test/banner.jpg", NOW))
                .when(new TopicCommand.UpdateTopicBannerUrlCommand(TOPIC_ID, OWNER, null))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicBannerUrlUpdatedEvent.class,
                                e ->
                                        ((TopicEvent.TopicBannerUrlUpdatedEvent) e).bannerUrl()
                                                == null));
    }

    @Test
    void updateBannerUrl_withTooLongValue_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicBannerUrlCommand(
                                TOPIC_ID, OWNER, "https://x.test/" + "a".repeat(1100)))
                .expectException(TopicProblems.TopicBannerUrlTooLongProblem.class);
    }

    @Test
    void updateBannerUrl_byNonOwner_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicBannerUrlCommand(
                                TOPIC_ID, "intruder", "https://media.test/banner.jpg"))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    @Test
    void updatePictogramUrl_appliesEvent() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicPictogramUrlCommand(
                                TOPIC_ID, OWNER, "https://media.test/pictogram.png"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicPictogramUrlUpdatedEvent.class,
                                e -> "https://media.test/pictogram.png"
                                        .equals(((TopicEvent.TopicPictogramUrlUpdatedEvent) e).pictogramUrl())));
    }

    @Test
    void createTopic_withPictogram_carriesIt() {
        fixture.givenNoPriorActivity()
                .when(
                        new TopicCommand.CreateTopicCommand(
                                TOPIC_ID,
                                Map.of(Language.FR, "Cinéma"),
                                null,
                                TopicCategory.MOVIES,
                                null,
                                "https://media.test/pictogram.svg",
                                null,
                                OWNER))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicCreatedEvent.class,
                                e -> "https://media.test/pictogram.svg"
                                        .equals(((TopicEvent.TopicCreatedEvent) e).pictogramUrl())));
    }

    @Test
    void updatePictogramUrl_withSameValue_isNoOp() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicPictogramUrlUpdatedEvent(
                                TOPIC_ID, OWNER, "https://media.test/pictogram.png", NOW))
                .when(
                        new TopicCommand.UpdateTopicPictogramUrlCommand(
                                TOPIC_ID, OWNER, "https://media.test/pictogram.png"))
                .expectNoEvents();
    }

    @Test
    void updatePictogramUrl_withNull_clearsIt() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicPictogramUrlUpdatedEvent(
                                TOPIC_ID, OWNER, "https://media.test/pictogram.png", NOW))
                .when(new TopicCommand.UpdateTopicPictogramUrlCommand(TOPIC_ID, OWNER, null))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicPictogramUrlUpdatedEvent.class,
                                e -> ((TopicEvent.TopicPictogramUrlUpdatedEvent) e).pictogramUrl() == null));
    }

    @Test
    void updatePictogramUrl_withTooLongValue_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicPictogramUrlCommand(
                                TOPIC_ID, OWNER, "https://media.test/" + "a".repeat(1024)))
                .expectException(TopicProblems.TopicPictogramUrlTooLongProblem.class);
    }

    @Test
    void updatePictogramUrl_byNonOwner_rejects() {
        fixture.given(createdTopic())
                .when(
                        new TopicCommand.UpdateTopicPictogramUrlCommand(
                                TOPIC_ID, "intruder", "https://media.test/pictogram.png"))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    private TopicCommand.CreateTopicCommand createCommand(String name, String description) {
        return new TopicCommand.CreateTopicCommand(
                TOPIC_ID,
                Map.of(Language.FR, name),
                description,
                TopicCategory.MOVIES,
                null,
                null,
                null,
                OWNER);
    }

    @Test
    void deleteDraft_byCreator_deletesTheTopic() {
        fixture.given(createdTopic())
                .when(new TopicCommand.DeleteTopicCommand(TOPIC_ID, OWNER))
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(
                        org.axonframework.test.matchers.Matchers.exactSequenceOf(
                                org.axonframework.test.matchers.Matchers.messageWithPayload(
                                        org.hamcrest.Matchers.instanceOf(TopicEvent.TopicDeletedEvent.class)),
                                org.axonframework.test.matchers.Matchers.andNoMore()))
                .expectMarkedDeleted();
    }

    @Test
    void deletePublishedTopic_isRefused() {
        fixture.given(createdTopic(), new TopicEvent.TopicPublishedEvent(TOPIC_ID, OWNER, NOW))
                .when(new TopicCommand.DeleteTopicCommand(TOPIC_ID, OWNER))
                .expectException(TopicProblems.TopicNotInDraftProblem.class);
    }

    @Test
    void deleteDraft_byAnotherMember_isRefused() {
        fixture.given(createdTopic())
                .when(new TopicCommand.DeleteTopicCommand(TOPIC_ID, "someone-else"))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    private TopicEvent.TopicCreatedEvent createdTopic() {
        return new TopicEvent.TopicCreatedEvent(
                TOPIC_ID,
                Map.of(Language.FR, "Cinéma"),
                "Tout le cinéma",
                TopicCategory.MOVIES,
                null,
                null,
                null,
                OWNER,
                NOW);
    }

    @Test
    void writerCannotManageTeam() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicMemberSetEvent(
                                TOPIC_ID,
                                "writer",
                                com.joutio.theme.domain.model.TopicTeamRole.WRITER,
                                OWNER,
                                NOW))
                .when(
                        new TopicCommand.SetTopicMemberCommand(
                                TOPIC_ID,
                                "writer",
                                "other",
                                com.joutio.theme.domain.model.TopicTeamRole.WRITER))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    @Test
    void creatorCannotBeRemoved() {
        fixture.given(createdTopic())
                .when(new TopicCommand.RemoveTopicMemberCommand(TOPIC_ID, OWNER, OWNER))
                .expectException(TopicProblems.TopicNotOwnerProblem.class);
    }

    @Test
    void adminCanManageTopic() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicMemberSetEvent(
                                TOPIC_ID,
                                "admin",
                                com.joutio.theme.domain.model.TopicTeamRole.ADMIN,
                                OWNER,
                                NOW))
                .when(
                        new TopicCommand.UpdateTopicNameCommand(
                                TOPIC_ID, "admin", Language.FR, "Nouveau nom"))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicNameUpdatedEvent.class, e -> true));
    }

    @Test
    void repeatedMemberRoleIsNoOp() {
        fixture.given(
                        createdTopic(),
                        new TopicEvent.TopicMemberSetEvent(
                                TOPIC_ID,
                                "writer",
                                com.joutio.theme.domain.model.TopicTeamRole.WRITER,
                                OWNER,
                                NOW))
                .when(
                        new TopicCommand.SetTopicMemberCommand(
                                TOPIC_ID,
                                OWNER,
                                "writer",
                                com.joutio.theme.domain.model.TopicTeamRole.WRITER))
                .expectNoEvents();
    }

    @Test
    void ownerCanUnpublishTopic() {
        fixture.given(createdTopic(), new TopicEvent.TopicPublishedEvent(TOPIC_ID, OWNER, NOW))
                .when(new TopicCommand.UnpublishTopicCommand(TOPIC_ID, OWNER))
                .expectEventsMatching(
                        JoutioAxonMatchers.singlePayloadMatching(
                                TopicEvent.TopicDraftedEvent.class,
                                e ->
                                        "AUTHOR_REQUEST"
                                                .equals(
                                                        ((TopicEvent.TopicDraftedEvent) e)
                                                                .reason())));
    }
}

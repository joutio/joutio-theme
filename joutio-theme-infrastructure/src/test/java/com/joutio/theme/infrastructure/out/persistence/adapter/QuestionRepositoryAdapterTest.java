package com.joutio.theme.infrastructure.out.persistence.adapter;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionChoice;
import com.joutio.theme.domain.model.QuestionDifficulty;
import com.joutio.theme.domain.model.QuestionStatus;
import com.joutio.theme.infrastructure.out.persistence.entity.QuestionContentEntity;
import com.joutio.theme.infrastructure.out.persistence.entity.QuestionEntity;
import com.joutio.theme.infrastructure.out.persistence.repository.QuestionContentJpaRepository;
import com.joutio.theme.infrastructure.out.persistence.repository.QuestionJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tirage des questions (2026-11-16) : aucun dédoublonnage par contenu. Un thème peut porter
 * plusieurs questions au même énoncé, avec ou sans illustration (« Quel est ce pays ? ») : chacune
 * est une question à part entière et le tirage en rend autant que demandé.
 */
class QuestionRepositoryAdapterTest {

    private final QuestionJpaRepository questionJpaRepository = mock(QuestionJpaRepository.class);
    private final QuestionContentJpaRepository contentJpaRepository =
            mock(QuestionContentJpaRepository.class);
    private final QuestionRepositoryAdapter adapter =
            new QuestionRepositoryAdapter(questionJpaRepository, contentJpaRepository);

    @Test
    void draw_keeps_every_question_sharing_the_same_text_without_illustration() {
        QuestionEntity[] entities = new QuestionEntity[7];
        QuestionContentEntity[] contents = new QuestionContentEntity[7];
        for (int i = 0; i < 7; i++) {
            entities[i] = entity("q" + i, null);
            contents[i] = content("q" + i, "Quelle est la bonne orthographe ?");
        }
        givenDrawReturns(entities);
        givenContents(contents);

        List<Question> drawn = adapter.findRandomApprovedByTopicId(
                "topic-1", 7, Set.of(Language.FR), Set.of(), Set.of());

        assertThat(drawn).extracting(Question::questionId)
                .containsExactly("q0", "q1", "q2", "q3", "q4", "q5", "q6");
    }

    @Test
    void draw_keeps_questions_sharing_text_and_illustration() {
        givenDrawReturns(
                entity("q1", "wikipedia/flags/fr.png"),
                entity("q2", "wikipedia/flags/fr.png"),
                entity("q3", "wikipedia/flags/de.png"));
        givenContents(
                content("q1", "Quel est ce pays ?"),
                content("q2", "Quel est ce pays ?"),
                content("q3", "Quel est ce pays ?"));

        List<Question> drawn = adapter.findRandomApprovedByTopicId(
                "topic-1", 7, Set.of(Language.FR), Set.of(), Set.of());

        assertThat(drawn).extracting(Question::questionId).containsExactly("q1", "q2", "q3");
    }

    @Test
    void draw_requests_exactly_the_round_count() {
        givenDrawReturns();
        givenContents();

        adapter.findRandomApprovedByTopicId("topic-1", 7, Set.of(Language.FR), Set.of(), Set.of());

        verify(questionJpaRepository)
                .findRandomApprovedByTopicId(eq("topic-1"), eq(7), anyList(), eq(1L), eq(""), eq(""));
    }

    private void givenDrawReturns(QuestionEntity... entities) {
        when(questionJpaRepository.findRandomApprovedByTopicId(
                anyString(), anyInt(), anyList(), anyLong(), anyString(), anyString()))
                .thenReturn(List.of(entities));
    }

    private void givenContents(QuestionContentEntity... contents) {
        when(contentJpaRepository.findByQuestionIdIn(anyList())).thenReturn(List.of(contents));
    }

    private static QuestionEntity entity(String questionId, String imageUrl) {
        QuestionEntity entity = new QuestionEntity();
        entity.setQuestionId(questionId);
        entity.setTopicId("topic-1");
        entity.setImageUrl(imageUrl);
        entity.setCorrectAnswer(QuestionChoice.A);
        entity.setStatus(QuestionStatus.APPROVED);
        entity.setDifficulty(QuestionDifficulty.HARD);
        entity.setCreatorId("creator-1");
        return entity;
    }

    private static QuestionContentEntity content(String questionId, String text) {
        QuestionContentEntity entity = new QuestionContentEntity();
        entity.setQuestionId(questionId);
        entity.setLanguage(Language.FR.code());
        entity.setText(text);
        entity.setAnswers(Map.of(
                QuestionChoice.A, "France",
                QuestionChoice.B, "Allemagne",
                QuestionChoice.C, "Espagne",
                QuestionChoice.D, "Italie"));
        return entity;
    }
}

package com.joutio.theme.domain.port.out;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.microservice.core.infrastructure.in.api.request.SearchRequest;
import com.joutio.microservice.core.infrastructure.in.api.response.SearchResponse;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionAdminFilter;
import com.joutio.theme.domain.model.QuestionAdminPage;
import com.joutio.theme.domain.model.QuestionAdminSort;
import com.joutio.theme.domain.model.QuestionDifficulty;
import com.joutio.theme.domain.model.QuestionStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionRepositoryPort {

    void save(Question question);

    Optional<Question> findById(String questionId);

    List<Question> findByTopicId(String topicId);

    List<Question> findByIds(List<String> questionIds);

    QuestionAdminPage findAdminPage(QuestionAdminFilter filter,
                                    QuestionAdminSort sort,
                                    int page,
                                    int size);

    int countSuspended();

    int countArchived();

    int countApprovedByTopicId(String topicId);

    int countByTopicIdAndStatus(String topicId, QuestionStatus status);

    /** Nombre de questions approuvées disponibles dans toutes les langues demandées. */
    int countApprovedByTopicAndLanguages(String topicId, Set<Language> languages);

    /**
     * Questions approuvées aléatoires, disponibles dans toutes les langues demandées.
     * {@code difficulties} et {@code excludeQuestionIds} sont des préférences de tri best-effort
     * (jamais-jouées &gt; difficulté cible &gt; difficulté inconnue &gt; autres).
     */
    List<Question> findRandomApprovedByTopicId(String topicId, int count, Set<Language> languages,
                                               Set<QuestionDifficulty> difficulties,
                                               Set<String> excludeQuestionIds);

    SearchResponse<Question> findAll(SearchRequest request);
}

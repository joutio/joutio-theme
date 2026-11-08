package com.joutio.theme.domain.model;

/** Effective permissions: a stored administrator without Premium acts as a writer. */
public record TopicPermissions(
        TopicTeamRole role,
        boolean canContribute,
        boolean canManage,
        boolean canManageTeam,
        boolean canAddMembers) {
    public static TopicPermissions forMember(TopicTeamRole role, boolean premium) {
        boolean owner = role == TopicTeamRole.CREATOR;
        boolean manager = owner || role == TopicTeamRole.ADMIN && premium;
        return new TopicPermissions(role, role != null, manager, manager, manager && premium);
    }

    public boolean canEdit(Question question, String userId) {
        return canManage
                || canContribute
                        && userId.equals(question.creatorId())
                        && (question.status() == QuestionStatus.PENDING
                                || question.status() == QuestionStatus.REJECTED);
    }
}

package com.joutio.theme.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TopicPermissionsTest {
    @Test
    void freeCreatorKeepsContentButCannotAddMembers() {
        var p = TopicPermissions.forMember(TopicTeamRole.CREATOR, false);
        assertTrue(p.canManage());
        assertTrue(p.canManageTeam());
        assertFalse(p.canAddMembers());
    }

    @Test
    void freeAdminHasOnlyWriterRights() {
        var p = TopicPermissions.forMember(TopicTeamRole.ADMIN, false);
        assertTrue(p.canContribute());
        assertFalse(p.canManage());
        assertFalse(p.canManageTeam());
    }

    @Test
    void premiumAdminCanManage() {
        assertTrue(TopicPermissions.forMember(TopicTeamRole.ADMIN, true).canAddMembers());
    }

    @Test
    void strangerCannotContribute() {
        assertFalse(TopicPermissions.forMember(null, true).canContribute());
    }

    @Test
    void writerMayEditOnlyOwnPendingOrRejectedQuestions() {
        var p = TopicPermissions.forMember(TopicTeamRole.WRITER, false);
        for (var status : QuestionStatus.values()) {
            var q = Question.builder().creatorId("writer").status(status).build();
            assertEquals(
                    status == QuestionStatus.PENDING || status == QuestionStatus.REJECTED,
                    p.canEdit(q, "writer"));
            assertFalse(p.canEdit(q, "other"));
        }
    }
}

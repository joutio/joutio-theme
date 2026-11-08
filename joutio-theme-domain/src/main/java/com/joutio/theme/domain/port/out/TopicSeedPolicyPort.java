package com.joutio.theme.domain.port.out;

/** Manual editorial decisions must survive automatic seed completion. */
public interface TopicSeedPolicyPort {
    boolean isLocked(String topicId);

    void lock(String topicId);
}

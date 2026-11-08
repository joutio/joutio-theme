package com.joutio.theme.domain.model;

import java.util.Map;

public record TopicTeam(Map<String, TopicTeamRole> members) {}

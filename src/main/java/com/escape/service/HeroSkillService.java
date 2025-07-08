package com.escape.service;

import com.escape.entity.HeroSkill;

import java.util.List;

public interface HeroSkillService {
    List<HeroSkill> getHeroSkills(Long heroId);
    void saveOrUpdateHeroSkills(Long heroId, List<HeroSkill> skills);
}

package com.escape.service.impl;

import com.escape.entity.HeroSkill;
import com.escape.mapper.HeroSkillMapper;
import com.escape.service.HeroSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HeroSkillServiceImpl implements HeroSkillService {
    @Autowired
    private HeroSkillMapper heroSkillMapper;

    @Override
    public List<HeroSkill> getHeroSkills(Long heroId) {
        return heroSkillMapper.findByHeroId(heroId);
    }

    @Override
    @Transactional
    public void saveOrUpdateHeroSkills(Long heroId, List<HeroSkill> skills) {
        // 先删除原有技能
        heroSkillMapper.deleteByHeroId(heroId);
        // 批量插入新技能
        for (HeroSkill skill : skills) {
            skill.setId(null); // 保证自增
            skill.setHeroId(heroId);
            heroSkillMapper.insert(skill);
        }
    }
}

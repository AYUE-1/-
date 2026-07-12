package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petadoption.dto.AssessmentSubmitDTO;
import com.petadoption.entity.Assessment;
import com.petadoption.entity.AssessmentQuestion;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.AssessmentMapper;
import com.petadoption.mapper.AssessmentQuestionMapper;
import com.petadoption.service.AssessmentService;
import com.petadoption.vo.AssessmentResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentQuestionMapper questionMapper;
    private final AssessmentMapper assessmentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== 维度权重 =====
    private static final Map<String, Double> DIMENSION_WEIGHTS = Map.of(
        "living_space", 1.5,
        "time_availability", 2.0,
        "experience", 1.5,
        "budget", 1.5,
        "family_composition", 1.2,
        "activity_level", 1.3,
        "pet_preference", 1.0
    );

    // ===== 品种评分矩阵 (breed -> dimension -> baseline score 0-1) =====
    private static final Map<String, Map<String, Double>> BREED_MATRIX = new LinkedHashMap<>();

    static {
        // 猫咪品种
        BREED_MATRIX.put("中华田园猫", Map.of(
            "living_space", 0.9, "time_availability", 0.5, "experience", 0.7,
            "budget", 0.9, "family_composition", 0.7, "activity_level", 0.5, "pet_preference", 0.8));
        BREED_MATRIX.put("英短蓝猫", Map.of(
            "living_space", 0.7, "time_availability", 0.5, "experience", 0.6,
            "budget", 0.6, "family_composition", 0.8, "activity_level", 0.4, "pet_preference", 0.8));
        BREED_MATRIX.put("美短虎斑", Map.of(
            "living_space", 0.7, "time_availability", 0.6, "experience", 0.6,
            "budget", 0.6, "family_composition", 0.8, "activity_level", 0.7, "pet_preference", 0.7));
        BREED_MATRIX.put("布偶猫", Map.of(
            "living_space", 0.7, "time_availability", 0.7, "experience", 0.5,
            "budget", 0.4, "family_composition", 0.8, "activity_level", 0.3, "pet_preference", 0.8));
        BREED_MATRIX.put("暹罗猫", Map.of(
            "living_space", 0.7, "time_availability", 0.7, "experience", 0.6,
            "budget", 0.6, "family_composition", 0.6, "activity_level", 0.8, "pet_preference", 0.7));
        BREED_MATRIX.put("橘猫", Map.of(
            "living_space", 0.9, "time_availability", 0.5, "experience", 0.7,
            "budget", 0.8, "family_composition", 0.7, "activity_level", 0.5, "pet_preference", 0.8));
        BREED_MATRIX.put("三花猫", Map.of(
            "living_space", 0.8, "time_availability", 0.5, "experience", 0.7,
            "budget", 0.8, "family_composition", 0.7, "activity_level", 0.5, "pet_preference", 0.9));
        BREED_MATRIX.put("波斯猫", Map.of(
            "living_space", 0.6, "time_availability", 0.8, "experience", 0.4,
            "budget", 0.3, "family_composition", 0.5, "activity_level", 0.2, "pet_preference", 0.8));
        BREED_MATRIX.put("无毛猫", Map.of(
            "living_space", 0.5, "time_availability", 0.8, "experience", 0.3,
            "budget", 0.2, "family_composition", 0.4, "activity_level", 0.4, "pet_preference", 0.5));

        // 狗狗品种
        BREED_MATRIX.put("中华田园犬", Map.of(
            "living_space", 0.7, "time_availability", 0.6, "experience", 0.7,
            "budget", 0.8, "family_composition", 0.7, "activity_level", 0.6, "pet_preference", 0.7));
        BREED_MATRIX.put("金毛寻回犬", Map.of(
            "living_space", 0.4, "time_availability", 0.8, "experience", 0.5,
            "budget", 0.5, "family_composition", 0.9, "activity_level", 0.8, "pet_preference", 0.6));
        BREED_MATRIX.put("拉布拉多", Map.of(
            "living_space", 0.4, "time_availability", 0.7, "experience", 0.5,
            "budget", 0.5, "family_composition", 0.9, "activity_level", 0.8, "pet_preference", 0.5));
        BREED_MATRIX.put("柯基", Map.of(
            "living_space", 0.6, "time_availability", 0.6, "experience", 0.5,
            "budget", 0.5, "family_composition", 0.8, "activity_level", 0.7, "pet_preference", 0.7));
        BREED_MATRIX.put("泰迪/贵宾", Map.of(
            "living_space", 0.8, "time_availability", 0.6, "experience", 0.4,
            "budget", 0.5, "family_composition", 0.7, "activity_level", 0.6, "pet_preference", 0.5));
        BREED_MATRIX.put("比熊", Map.of(
            "living_space", 0.9, "time_availability", 0.6, "experience", 0.4,
            "budget", 0.5, "family_composition", 0.8, "activity_level", 0.5, "pet_preference", 0.5));
        BREED_MATRIX.put("柴犬", Map.of(
            "living_space", 0.6, "time_availability", 0.6, "experience", 0.4,
            "budget", 0.6, "family_composition", 0.5, "activity_level", 0.7, "pet_preference", 0.6));
        BREED_MATRIX.put("哈士奇", Map.of(
            "living_space", 0.2, "time_availability", 0.9, "experience", 0.3,
            "budget", 0.4, "family_composition", 0.4, "activity_level", 0.9, "pet_preference", 0.4));
        BREED_MATRIX.put("边境牧羊犬", Map.of(
            "living_space", 0.3, "time_availability", 0.9, "experience", 0.3,
            "budget", 0.4, "family_composition", 0.6, "activity_level", 0.9, "pet_preference", 0.4));
        BREED_MATRIX.put("法国斗牛犬", Map.of(
            "living_space", 0.9, "time_availability", 0.4, "experience", 0.5,
            "budget", 0.5, "family_composition", 0.7, "activity_level", 0.3, "pet_preference", 0.5));
        BREED_MATRIX.put("博美", Map.of(
            "living_space", 0.9, "time_availability", 0.5, "experience", 0.4,
            "budget", 0.5, "family_composition", 0.6, "activity_level", 0.5, "pet_preference", 0.5));

        // 其他小宠
        BREED_MATRIX.put("仓鼠", Map.of(
            "living_space", 1.0, "time_availability", 0.3, "experience", 0.9,
            "budget", 1.0, "family_composition", 0.9, "activity_level", 0.1, "pet_preference", 0.6));
        BREED_MATRIX.put("兔子", Map.of(
            "living_space", 0.9, "time_availability", 0.4, "experience", 0.7,
            "budget", 0.8, "family_composition", 0.8, "activity_level", 0.3, "pet_preference", 0.7));
        BREED_MATRIX.put("豚鼠(荷兰猪)", Map.of(
            "living_space", 0.9, "time_availability", 0.4, "experience", 0.8,
            "budget", 0.9, "family_composition", 0.8, "activity_level", 0.2, "pet_preference", 0.6));
        BREED_MATRIX.put("鹦鹉", Map.of(
            "living_space", 0.8, "time_availability", 0.6, "experience", 0.5,
            "budget", 0.7, "family_composition", 0.5, "activity_level", 0.3, "pet_preference", 0.5));
    }

    // 品种描述
    private static final Map<String, String> BREED_DESC = Map.ofEntries(
        Map.entry("中华田园猫", "适应力强、体质好、聪明独立，是新手的最佳选择"),
        Map.entry("英短蓝猫", "性格温顺安静，圆脸可爱，适合公寓饲养"),
        Map.entry("美短虎斑", "活泼聪明、亲人友善，适合有小孩的家庭"),
        Map.entry("布偶猫", "性格温柔粘人，被称为\"小狗猫\"，需要较多陪伴"),
        Map.entry("暹罗猫", "社交达人，喜欢互动玩耍，需要主人有较多时间"),
        Map.entry("橘猫", "性格温厚、贪吃可爱，是中华田园猫中的明星"),
        Map.entry("三花猫", "温顺亲人、颜值出众，多为母猫性格柔和"),
        Map.entry("波斯猫", "优雅高贵、性格安静，需要精心打理毛发"),
        Map.entry("无毛猫", "性格粘人、聪明活泼，需要特殊的皮肤护理"),
        Map.entry("中华田园犬", "忠诚健康、适应力强、看家好手，适合各类家庭"),
        Map.entry("金毛寻回犬", "温顺友好、智商高，是理想的家庭伴侣犬"),
        Map.entry("拉布拉多", "友善活泼、服从性好，适合有小孩的家庭"),
        Map.entry("柯基", "短腿萌物、性格开朗、精力充沛，需要适量运动"),
        Map.entry("泰迪/贵宾", "聪明不掉毛、活泼可爱，适合公寓饲养但需要定期美容"),
        Map.entry("比熊", "温顺粘人、不掉毛，适合公寓饲养但需要定期美容"),
        Map.entry("柴犬", "独立倔强、忠诚勇敢，表情丰富是行走的表情包"),
        Map.entry("哈士奇", "精力王者、搞笑担当，需要超大运动量和耐心"),
        Map.entry("边境牧羊犬", "智商第一、运动需求极高，需要大量互动和空间"),
        Map.entry("法国斗牛犬", "安静温顺、适合公寓，但需注意呼吸道健康"),
        Map.entry("博美", "小巧可爱、活泼机警，适合公寓但爱叫需训练"),
        Map.entry("仓鼠", "占用空间小、成本低、适合忙碌的上班族和学生"),
        Map.entry("兔子", "温顺安静、适合室内饲养，需要耐心和细心"),
        Map.entry("豚鼠(荷兰猪)", "温顺可爱、社交需求高，建议成对饲养"),
        Map.entry("鹦鹉", "聪明有趣、寿命长，需要大量互动和空间")
    );

    @Override
    public List<AssessmentQuestion> getQuestions(String petCategory) {
        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<>();
        if (petCategory != null && !petCategory.isEmpty() && !"ALL".equals(petCategory)) {
            wrapper.and(w -> w.eq(AssessmentQuestion::getPetCategory, petCategory)
                    .or().eq(AssessmentQuestion::getPetCategory, "ALL"));
        }
        wrapper.orderByAsc(AssessmentQuestion::getSortOrder);
        return questionMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public AssessmentResultVO submitAssessment(Long userId, AssessmentSubmitDTO dto) {
        // 1. 获取题目
        List<AssessmentQuestion> questions = getQuestions(dto.getPetCategory());
        if (questions.isEmpty()) throw new BusinessException("暂无测评题目");

        // 2. 计算用户各维度得分
        Map<String, Double> dimensionScores = calculateDimensionScores(questions, dto.getAnswers());

        // 3. 计算品种匹配度
        List<AssessmentResultVO.BreedRecommendation> recommendations = calculateBreedMatches(dimensionScores, dto.getPetCategory());

        // 4. 生成养护建议
        List<String> careTips = generateCareTips(dimensionScores);

        // 5. 养前 Checklist 评估
        AssessmentResultVO.ChecklistResult checklist = generateChecklist(dimensionScores);

        // 6. 计算总分
        double overallScore = dimensionScores.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.5) * 100;

        // 7. 构建结果
        AssessmentResultVO result = new AssessmentResultVO();
        result.setDimensionScores(dimensionScores);
        result.setRecommendations(recommendations);
        result.setCareTips(careTips);
        result.setChecklist(checklist);
        result.setOverallScore(Math.round(overallScore * 10.0) / 10.0);

        // 8. 保存到数据库（无论是否登录都保存）
        try {
            Assessment assessment = new Assessment();
            assessment.setUserId(userId);
            assessment.setAnswers(objectMapper.writeValueAsString(dto.getAnswers()));
            assessment.setResultData(objectMapper.writeValueAsString(result));
            assessment.setPetCategory(dto.getPetCategory());
            assessmentMapper.insert(assessment);
            result.setId(assessment.getId());
        } catch (Exception e) {
            // 保存失败不影响返回结果
        }

        return result;
    }

    @Override
    public AssessmentResultVO getResult(Long assessmentId) {
        Assessment assessment = assessmentMapper.selectById(assessmentId);
        if (assessment == null) throw new BusinessException("测评记录不存在");
        try {
            return objectMapper.readValue(assessment.getResultData(), AssessmentResultVO.class);
        } catch (Exception e) {
            throw new BusinessException("测评结果解析失败");
        }
    }

    @Override
    public List<AssessmentResultVO> getHistory(Long userId) {
        List<Assessment> assessments = assessmentMapper.selectList(
            new LambdaQueryWrapper<Assessment>()
                .eq(Assessment::getUserId, userId)
                .orderByDesc(Assessment::getCreatedAt)
        );
        return assessments.stream().map(a -> {
            try {
                AssessmentResultVO vo = objectMapper.readValue(a.getResultData(), AssessmentResultVO.class);
                vo.setId(a.getId());
                return vo;
            } catch (Exception e) { return null; }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ===== 核心算法 =====

    private Map<String, Double> calculateDimensionScores(List<AssessmentQuestion> questions,
                                                          List<AssessmentSubmitDTO.AnswerItem> answers) {
        Map<Long, Integer> answerMap = answers.stream()
            .collect(Collectors.toMap(AssessmentSubmitDTO.AnswerItem::getQuestionId,
                                      AssessmentSubmitDTO.AnswerItem::getSelectedIndex));

        // 按维度分组计算加权平均分
        Map<String, Double> dimensionWeightedSum = new LinkedHashMap<>();
        Map<String, Double> dimensionTotalWeight = new LinkedHashMap<>();

        for (AssessmentQuestion q : questions) {
            Integer selectedIndex = answerMap.get(q.getId());
            if (selectedIndex == null) continue;

            double optionScore = getOptionScore(q.getOptions(), selectedIndex);
            String dim = q.getDimension();
            double qWeight = q.getWeight() != null ? q.getWeight() : 1.0;

            dimensionWeightedSum.merge(dim, optionScore * qWeight, Double::sum);
            dimensionTotalWeight.merge(dim, qWeight, Double::sum);
        }

        // 归一化到 0-1
        Map<String, Double> result = new LinkedHashMap<>();
        for (String dim : dimensionWeightedSum.keySet()) {
            double total = dimensionTotalWeight.getOrDefault(dim, 1.0);
            result.put(dim, Math.min(1.0, dimensionWeightedSum.get(dim) / total));
        }
        return result;
    }

    private double getOptionScore(String optionsJson, int index) {
        try {
            List<Map<String, Object>> options = objectMapper.readValue(optionsJson,
                new TypeReference<List<Map<String, Object>>>() {});
            if (index >= 0 && index < options.size()) {
                Object score = options.get(index).get("score");
                if (score instanceof Number) return ((Number) score).doubleValue();
            }
        } catch (Exception ignored) {}
        return 0.5;
    }

    private List<AssessmentResultVO.BreedRecommendation> calculateBreedMatches(
            Map<String, Double> userScores, String petCategory) {

        List<AssessmentResultVO.BreedRecommendation> results = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> entry : BREED_MATRIX.entrySet()) {
            String breedName = entry.getKey();
            Map<String, Double> breedScores = entry.getValue();

            // 根据用户选择的宠物类别过滤
            if (petCategory != null && !petCategory.isEmpty() && !"ALL".equals(petCategory)) {
                boolean isCat = List.of("中华田园猫","英短蓝猫","美短虎斑","布偶猫","暹罗猫","橘猫","三花猫","波斯猫","无毛猫").contains(breedName);
                boolean isDog = List.of("中华田园犬","金毛寻回犬","拉布拉多","柯基","泰迪/贵宾","比熊","柴犬","哈士奇","边境牧羊犬","法国斗牛犬","博美").contains(breedName);
                if ("CAT".equals(petCategory) && !isCat) continue;
                if ("DOG".equals(petCategory) && !isDog) continue;
            }

            // 计算匹配度: 1 - sum(|breed_i - user_i| * dimWeight_i) / sum(dimWeight_i)
            double weightedDiff = 0;
            double totalWeight = 0;
            for (String dim : userScores.keySet()) {
                double dimWeight = DIMENSION_WEIGHTS.getOrDefault(dim, 1.0);
                double userScore = userScores.getOrDefault(dim, 0.5);
                double breedScore = breedScores.getOrDefault(dim, 0.5);
                weightedDiff += Math.abs(breedScore - userScore) * dimWeight;
                totalWeight += dimWeight;
            }
            double matchScore = totalWeight > 0 ? (1.0 - weightedDiff / totalWeight) * 100 : 50;
            matchScore = Math.max(0, Math.min(100, matchScore));

            AssessmentResultVO.BreedRecommendation rec = new AssessmentResultVO.BreedRecommendation();
            rec.setBreedName(breedName);
            rec.setBreedDesc(BREED_DESC.getOrDefault(breedName, ""));
            rec.setMatchScore(Math.round(matchScore * 10.0) / 10.0);
            rec.setSuitableReason(generateSuitableReason(breedName, userScores, breedScores));
            rec.setTags(generateBreedTags(breedName, breedScores));
            results.add(rec);
        }

        // 按匹配度降序排序，取前5
        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results.stream().limit(5).collect(Collectors.toList());
    }

    private String generateSuitableReason(String breedName, Map<String, Double> userScores,
                                           Map<String, Double> breedScores) {
        // 找出匹配最好的维度
        String bestDim = null;
        double bestMatch = 0;
        for (String dim : userScores.keySet()) {
            double diff = 1.0 - Math.abs(breedScores.getOrDefault(dim, 0.5) - userScores.getOrDefault(dim, 0.5));
            if (diff > bestMatch) {
                bestMatch = diff;
                bestDim = dim;
            }
        }
        Map<String, String> dimNames = Map.of(
            "living_space", "居住条件", "time_availability", "时间投入",
            "experience", "养宠经验", "budget", "预算水平",
            "family_composition", "家庭环境", "activity_level", "活跃程度",
            "pet_preference", "偏好匹配"
        );
        return "在" + dimNames.getOrDefault(bestDim, "综合") + "方面与您高度匹配";
    }

    private List<String> generateBreedTags(String breedName, Map<String, Double> breedScores) {
        List<String> tags = new ArrayList<>();
        if (breedScores.getOrDefault("experience", 0.5) > 0.7) tags.add("适合新手");
        if (breedScores.getOrDefault("activity_level", 0.5) > 0.7) tags.add("运动量大");
        if (breedScores.getOrDefault("activity_level", 0.5) < 0.4) tags.add("安静陪伴");
        if (breedScores.getOrDefault("living_space", 0.5) > 0.8) tags.add("公寓友好");
        if (breedScores.getOrDefault("family_composition", 0.5) > 0.8) tags.add("亲人和善");
        if (breedScores.getOrDefault("budget", 0.5) > 0.8) tags.add("经济实惠");
        if (breedScores.getOrDefault("time_availability", 0.5) < 0.4) tags.add("独立省心");
        return tags;
    }

    private List<String> generateCareTips(Map<String, Double> scores) {
        List<String> tips = new ArrayList<>();

        if (scores.getOrDefault("living_space", 0.5) < 0.4) {
            tips.add("🏠 居住空间较小，建议选择体型小、活动需求低的宠物（如猫、仓鼠、兔子）");
        }
        if (scores.getOrDefault("time_availability", 0.5) < 0.4) {
            tips.add("⏰ 陪伴时间有限，建议选择独立性强的宠物（如猫咪）或考虑领养成对宠物");
        }
        if (scores.getOrDefault("experience", 0.5) < 0.4) {
            tips.add("📚 新手养宠建议从适应力强的品种开始（如中华田园猫/犬），领养前多学习养护知识");
        }
        if (scores.getOrDefault("budget", 0.5) < 0.4) {
            tips.add("💰 预算有限的话，领养代替购买，中华田园猫/犬体质好、开销低");
        }
        if (scores.getOrDefault("family_composition", 0.5) < 0.4) {
            tips.add("👨‍👩‍👧 家中有小孩建议选择性格温顺、耐心的品种（如金毛、布偶猫）");
        }
        if (scores.getOrDefault("activity_level", 0.5) < 0.3) {
            tips.add("🛋️ 您偏好安静，建议选择性格温顺的品种（如英短、法斗、波斯猫）");
        }

        if (tips.isEmpty()) {
            tips.add("🎉 您的条件很适合养宠！选择与您投缘的那个它吧！");
        }
        tips.add("💡 领养代替购买，给流浪动物一个温暖的家");

        return tips;
    }

    private AssessmentResultVO.ChecklistResult generateChecklist(Map<String, Double> scores) {
        AssessmentResultVO.ChecklistResult checklist = new AssessmentResultVO.ChecklistResult();
        List<String> passed = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (scores.getOrDefault("living_space", 0.5) >= 0.4) passed.add("居住空间满足基本养宠需求");
        else warnings.add("居住空间可能不足，请确认房东/物业允许养宠");

        if (scores.getOrDefault("time_availability", 0.5) >= 0.4) passed.add("有足够时间陪伴宠物");
        else warnings.add("陪伴时间可能不足，宠物需要日常关注和互动");

        if (scores.getOrDefault("experience", 0.5) >= 0.3) passed.add("有一定的养宠知识储备");
        else warnings.add("建议在领养前学习基础养护知识（饮食、疫苗、疾病预防）");

        if (scores.getOrDefault("budget", 0.5) >= 0.4) passed.add("经济条件能满足宠物基本开销");
        else warnings.add("请确认有足够经济能力承担宠物食品、医疗等持续支出");

        if (scores.getOrDefault("family_composition", 0.5) >= 0.5) passed.add("家庭环境适合养宠");
        else warnings.add("请确保所有家庭成员都同意并准备好迎接新成员");

        int totalItems = passed.size() + warnings.size();
        int passedCount = passed.size();
        int score = totalItems > 0 ? (passedCount * 100 / totalItems) : 50;

        checklist.setReady(passedCount >= totalItems - 1);
        checklist.setScore(score);
        checklist.setPassedItems(passed);
        checklist.setWarningItems(warnings);

        return checklist;
    }
}

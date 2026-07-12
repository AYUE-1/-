package com.petadoption.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.*;
import com.petadoption.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PasswordEncoder passwordEncoder;
    private final AssessmentQuestionMapper questionMapper;

    @Override
    public void run(String... args) {
        initCategories();
        initAdmin();
        initShelter();
        initAssessmentQuestions();
    }

    private void initCategories() {
        String[][] categories = {
            {"猫咪", "各类猫咪待领养", "1"},
            {"狗狗", "各类狗狗待领养", "2"},
            {"其他", "兔子、仓鼠等其他小动物", "3"}
        };
        for (String[] c : categories) {
            Long count = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>().eq(Category::getName, c[0])
            );
            if (count == 0) {
                Category category = new Category();
                category.setName(c[0]);
                category.setDescription(c[1]);
                category.setSortOrder(Integer.parseInt(c[2]));
                categoryMapper.insert(category);
            }
        }
    }

    private void initAdmin() {
        Long adminCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );
        if (adminCount == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userMapper.insert(admin);
            System.out.println("管理员账号已创建: admin / admin123");
        }
    }

    private void initShelter() {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "shelter")
        );
        if (count == 0) {
            User shelter = new User();
            shelter.setUsername("shelter");
            shelter.setPassword(passwordEncoder.encode("shelter123"));
            shelter.setNickname("爱心救助站");
            shelter.setRole("SHELTER");
            shelter.setStatus(1);
            shelter.setShelterName("阳光宠物救助中心");
            shelter.setShelterAddress("北京市朝阳区某某路100号");
            shelter.setCertStatus("APPROVED");
            userMapper.insert(shelter);
            System.out.println("机构账号已创建: shelter / shelter123");
        }
    }

    private void initAssessmentQuestions() {
        Long count = questionMapper.selectCount(null);
        if (count > 0) return;

        // 维度1: 居住空间 (living_space)
        addQuestion("您的居住环境是？", "SINGLE_CHOICE", "living_space",
            "[{\"label\":\"自有房屋带院子\",\"score\":1.0},{\"label\":\"自有房屋无院子\",\"score\":0.7},{\"label\":\"租房(允许养宠)\",\"score\":0.4},{\"label\":\"租房(不确定)\",\"score\":0.2},{\"label\":\"学生宿舍/合租\",\"score\":0.1}]",
            "ALL", 1.5, 1);
        addQuestion("您家里的空间大小？", "SINGLE_CHOICE", "living_space",
            "[{\"label\":\"很大(120㎡+)\",\"score\":1.0},{\"label\":\"中等(60-120㎡)\",\"score\":0.7},{\"label\":\"较小(30-60㎡)\",\"score\":0.4},{\"label\":\"很小(30㎡以下)\",\"score\":0.2}]",
            "ALL", 1.0, 2);

        // 维度2: 时间充裕度 (time_availability)
        addQuestion("您每天能陪伴宠物的时间？", "SINGLE_CHOICE", "time_availability",
            "[{\"label\":\"4小时以上\",\"score\":1.0},{\"label\":\"2-4小时\",\"score\":0.7},{\"label\":\"1-2小时\",\"score\":0.4},{\"label\":\"不足1小时\",\"score\":0.1}]",
            "ALL", 2.0, 3);
        addQuestion("您每月会出差/旅行几天？", "SINGLE_CHOICE", "time_availability",
            "[{\"label\":\"几乎不出差\",\"score\":1.0},{\"label\":\"1-3天\",\"score\":0.7},{\"label\":\"4-7天\",\"score\":0.4},{\"label\":\"经常出差\",\"score\":0.2}]",
            "ALL", 1.0, 4);

        // 维度3: 养宠经验 (experience)
        addQuestion("您之前有养宠经验吗？", "SINGLE_CHOICE", "experience",
            "[{\"label\":\"有丰富经验\",\"score\":1.0},{\"label\":\"有一些经验\",\"score\":0.6},{\"label\":\"养过但不太会\",\"score\":0.3},{\"label\":\"完全没经验\",\"score\":0.1}]",
            "ALL", 1.5, 5);

        // 维度4: 预算 (budget)
        addQuestion("您每月愿意为宠物花费多少？", "SINGLE_CHOICE", "budget",
            "[{\"label\":\"1000元以上\",\"score\":1.0},{\"label\":\"500-1000元\",\"score\":0.7},{\"label\":\"200-500元\",\"score\":0.4},{\"label\":\"200元以下\",\"score\":0.2}]",
            "ALL", 1.5, 6);

        // 维度5: 家庭构成 (family_composition)
        addQuestion("您家里是否有小孩(12岁以下)？", "SINGLE_CHOICE", "family_composition",
            "[{\"label\":\"没有\",\"score\":1.0},{\"label\":\"有，年龄较大(6-12岁)\",\"score\":0.7},{\"label\":\"有，年龄较小(0-6岁)\",\"score\":0.3}]",
            "ALL", 1.2, 7);
        addQuestion("家里是否有其他宠物？", "SINGLE_CHOICE", "family_composition",
            "[{\"label\":\"没有\",\"score\":1.0},{\"label\":\"有猫\",\"score\":0.6},{\"label\":\"有狗\",\"score\":0.6},{\"label\":\"有多种宠物\",\"score\":0.4}]",
            "ALL", 0.8, 8);

        // 维度6: 活跃程度 (activity_level)
        addQuestion("您平时的运动习惯？", "SINGLE_CHOICE", "activity_level",
            "[{\"label\":\"天天运动\",\"score\":1.0},{\"label\":\"每周3-5次\",\"score\":0.8},{\"label\":\"偶尔运动\",\"score\":0.5},{\"label\":\"几乎不运动\",\"score\":0.2}]",
            "ALL", 1.3, 9);
        addQuestion("您希望宠物的活跃程度是？", "SINGLE_CHOICE", "activity_level",
            "[{\"label\":\"非常活跃，一起运动\",\"score\":1.0},{\"label\":\"适度活跃\",\"score\":0.7},{\"label\":\"安静陪伴型\",\"score\":0.3}]",
            "ALL", 1.0, 10);

        // 维度7: 宠物偏好 (pet_preference)
        addQuestion("您能接受宠物掉毛吗？", "SINGLE_CHOICE", "pet_preference",
            "[{\"label\":\"完全接受\",\"score\":1.0},{\"label\":\"可以接受\",\"score\":0.7},{\"label\":\"不太能接受\",\"score\":0.3},{\"label\":\"完全不能接受\",\"score\":0.1}]",
            "ALL", 0.8, 11);
        addQuestion("您偏好的宠物体型？", "SINGLE_CHOICE", "pet_preference",
            "[{\"label\":\"大型\",\"score\":1.0},{\"label\":\"中型\",\"score\":0.7},{\"label\":\"小型\",\"score\":0.4},{\"label\":\"都可以\",\"score\":0.8}]",
            "ALL", 1.0, 12);

        System.out.println("测评题目已初始化: 12道题目");
    }

    private void addQuestion(String text, String type, String dimension, String options, String category, double weight, int sort) {
        AssessmentQuestion q = new AssessmentQuestion();
        q.setQuestionText(text);
        q.setQuestionType(type);
        q.setDimension(dimension);
        q.setOptions(options);
        q.setPetCategory(category);
        q.setWeight(weight);
        q.setSortOrder(sort);
        questionMapper.insert(q);
    }
}

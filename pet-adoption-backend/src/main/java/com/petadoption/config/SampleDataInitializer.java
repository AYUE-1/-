package com.petadoption.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.*;
import com.petadoption.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class SampleDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;
    private final AdoptionMapper adoptionMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final PasswordEncoder passwordEncoder;

    // 使用 Unsplash 真实宠物图片（稳定、免费、无需 API Key）
    private static final String CAT1 = "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=600&h=400&fit=crop";
    private static final String CAT1_2 = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600&h=400&fit=crop";
    private static final String CAT2 = "https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=600&h=400&fit=crop";
    private static final String CAT2_2 = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600&h=400&fit=crop";
    private static final String CAT3 = "https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=600&h=400&fit=crop";
    private static final String CAT3_2 = "https://images.unsplash.com/photo-1526336024174-e58f5cdd8e13?w=600&h=400&fit=crop";
    private static final String CAT4 = "https://images.unsplash.com/photo-1577023311546-cdc07a8454d6?w=600&h=400&fit=crop";
    private static final String CAT5 = "https://images.unsplash.com/photo-1555685812-4b943f1cb0eb?w=600&h=400&fit=crop";
    private static final String DOG1 = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600&h=400&fit=crop";
    private static final String DOG1_2 = "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600&h=400&fit=crop";
    private static final String DOG2 = "https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=600&h=400&fit=crop";
    private static final String DOG3 = "https://images.unsplash.com/photo-1618173745201-8e3b6d5b6d5e?w=600&h=400&fit=crop";
    private static final String DOG4 = "https://images.unsplash.com/photo-1601758125946-6ec2ef64daf8?w=600&h=400&fit=crop";
    private static final String DOG5 = "https://images.unsplash.com/photo-1583511666372-62e5a7ff251c?w=600&h=400&fit=crop";
    private static final String OTHER1 = "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600&h=400&fit=crop";
    private static final String OTHER2 = "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=600&h=400&fit=crop";

    @Override
    public void run(String... args) {
        if (petMapper.selectCount(null) > 2) {
            return;
        }

        log.info(">>> 开始生成样本数据...");

        // ========== 普通用户 ==========
        Long uid2 = createUser("zhangsan", "123456", "张三", "zhangsan@qq.com", "13800001111", "USER");
        Long uid3 = createUser("lisi", "123456", "李四", "lisi@qq.com", "13800002222", "USER");
        Long uid4 = createUser("wangwu", "123456", "王五", "wangwu@qq.com", "13800003333", "USER");
        Long uid5 = createUser("xiaomei", "123456", "小美", "xiaomei@qq.com", "13800004444", "USER");

        // ========== 猫咪 (categoryId=1) ==========
        Long cat1 = createPet("小橘", 1L, "橘猫", "6个月", "MALE", "SMALL",
                "橙色", "健康已驱虫", "已完成猫三联", 0,
                "活泼可爱的橘猫，喜欢和人互动，性格温顺粘人。在小区救助的流浪猫，现已完全适应家庭生活。喜欢玩逗猫棒，是个小吃货。",
                CAT1, "AVAILABLE", uid2);
        addPetImages(cat1, CAT1, CAT1_2);

        Long cat2 = createPet("奶茶", 1L, "英短蓝猫", "1岁", "FEMALE", "MEDIUM",
                "灰蓝色", "健康已绝育", "已完成猫三联+狂犬", 1,
                "温柔的英短蓝猫，圆脸大眼非常可爱。性格安静优雅，喜欢在窗边晒太阳，不吵不闹，适合上班族领养。已经绝育。",
                CAT2, "AVAILABLE", uid2);
        addPetImages(cat2, CAT2, CAT2_2);

        Long cat3 = createPet("雪球", 1L, "布偶猫", "8个月", "FEMALE", "MEDIUM",
                "白色", "健康活泼", "已完成猫三联", 0,
                "颜值超高的布偶猫，蓝眼睛像宝石一样美丽。性格超级粘人，喜欢被抱着，走到哪跟到哪。毛量丰厚，需要定期梳理。",
                CAT3, "AVAILABLE", uid3);
        addPetImages(cat3, CAT3, CAT3_2);

        Long cat4 = createPet("小黑", 1L, "中华田园猫", "3个月", "MALE", "SMALL",
                "黑色", "健康已驱虫", "已完成第一针疫苗", 0,
                "全身乌黑发亮，四只小爪子却是白色的，像穿了小袜子。特别亲人，一叫就来，会用猫砂盆，生活习惯很好。",
                CAT4, "AVAILABLE", uid3);

        Long cat5 = createPet("年糕", 1L, "暹罗猫", "2岁", "MALE", "MEDIUM",
                "海豹色", "健康已绝育", "已完成全部疫苗", 1,
                "成熟的暹罗猫，非常聪明，能听懂自己的名字。喜欢和人交流，叫声很有特点。对主人忠诚，适合有养猫经验的家庭。",
                CAT5, "ADOPTED", uid2);

        // ========== 狗狗 (categoryId=2) ==========
        Long dog1 = createPet("旺财", 2L, "金毛寻回犬", "1岁半", "MALE", "LARGE",
                "金黄色", "健康已驱虫", "已完成全部疫苗", 0,
                "热情开朗的金毛，对人友善，非常适合做家庭伴侣犬。会基本的服从指令（坐下、握手、趴下），喜欢玩球和游泳。精力充沛，需要每天足够的运动量。",
                DOG1, "AVAILABLE", uid3);
        addPetImages(dog1, DOG1, DOG1_2);

        Long dog2 = createPet("豆豆", 2L, "柯基", "8个月", "FEMALE", "SMALL",
                "黄白双色", "健康活泼", "已完成全部疫苗", 0,
                "可爱的柯基，小短腿大屁股，走起路来特别萌。性格非常友好，喜欢和人玩耍，对小朋友特别有耐心。已经学会定点大小便。",
                DOG2, "AVAILABLE", uid4);

        Long dog3 = createPet("小白", 2L, "萨摩耶", "2岁", "FEMALE", "LARGE",
                "纯白色", "健康已绝育", "已完成全部疫苗", 1,
                "漂亮的萨摩耶，笑容甜美，性格温顺。喜欢和人亲近，不爱叫，适合公寓饲养。需要定期梳毛保持毛发整洁。已经绝育。",
                DOG3, "AVAILABLE", uid4);
        addPetImages(dog3, DOG3);

        Long dog4 = createPet("阿福", 2L, "拉布拉多", "3岁", "MALE", "LARGE",
                "黑色", "健康已绝育", "已完成全部疫苗", 1,
                "成熟的拉布拉多，性格沉稳，非常聪明。导盲犬后代，服从性极好，会多种指令。适合有庭院的家庭或者喜欢户外运动的主人。",
                DOG4, "RESERVED", uid2);

        Long dog5 = createPet("花花", 2L, "泰迪", "1岁", "FEMALE", "SMALL",
                "棕色", "健康已驱虫", "已完成全部疫苗", 0,
                "可爱的泰迪，不掉毛，适合对毛发过敏的家庭。性格活泼粘人，喜欢被抱着，会撒娇。已经学会在尿垫上大小便。",
                DOG5, "AVAILABLE", uid5);

        // ========== 其他小动物 (categoryId=3) ==========
        Long other1 = createPet("团团", 3L, "荷兰垂耳兔", "4个月", "FEMALE", "SMALL",
                "灰白色", "健康", "无需疫苗", 0,
                "可爱的荷兰垂耳兔，耳朵软软地垂在两边，非常可爱。性格温顺，喜欢被人抚摸。会用兔厕所，吃兔粮和干草，饲养简单。",
                OTHER1, "AVAILABLE", uid5);

        Long other2 = createPet("小仓", 3L, "金丝熊仓鼠", "2个月", "MALE", "SMALL",
                "金色", "健康活泼", "无需疫苗", 0,
                "金丝熊仓鼠，体型比普通仓鼠大一些，性格温顺不咬人。已经习惯被手抓，适合作为小朋友的入门宠物。配全套笼子和跑轮。",
                OTHER2, "AVAILABLE", uid3);

        // ========== 领养申请 ==========
        createAdoption(cat1, uid4, "李四", "420101199508120011", "13800002222",
                "湖北省武汉市洪山区珞喻路1037号", "租房", 0, 1, "公司职员", "5000-8000",
                "我从小就喜欢猫，现在工作稳定了想养一只陪伴我。家里已经准备好了猫砂盆、猫粮和猫爬架。",
                "ADMIN_APPROVED", uid2);
        createAdoption(cat2, uid5, "小美", "510101199612100022", "13800004444",
                "四川省成都市武侯区人民南路四段12号", "自有", 1, 1, "设计师", "8000-15000",
                "我家有一只已经绝育的公猫，想再领养一只给它作伴。有丰富的养猫经验，会好好照顾奶茶。",
                "PENDING", null);
        createAdoption(dog1, uid5, "小美", "510101199612100022", "13800004444",
                "四川省成都市武侯区人民南路四段12号", "自有", 1, 1, "设计师", "8000-15000",
                "我家有个大院子，非常适合养大型犬。之前养过一只拉布拉多，因为年纪大了走了，现在想再养一只。",
                "PENDING", null);
        createAdoption(dog4, uid4, "李四", "420101199508120011", "13800002222",
                "湖北省武汉市洪山区珞喻路1037号", "租房", 0, 1, "公司职员", "5000-8000",
                "想领养阿福，虽然住公寓但我每天都会带它出去运动跑步。",
                "REJECTED", uid2);

        // ========== 收藏 ==========
        createFavorite(uid3, cat1);
        createFavorite(uid3, cat2);
        createFavorite(uid4, dog1);
        createFavorite(uid4, cat1);
        createFavorite(uid5, cat2);
        createFavorite(uid5, dog2);
        createFavorite(uid5, dog3);
        createFavorite(uid3, dog2);

        // ========== 评论 ==========
        createComment(cat1, uid3, "小橘好可爱！我昨天去看过它，真的很亲人~");
        createComment(cat1, uid5, "请问小橘会用猫砂盆吗？");
        createCommentReply(cat1, uid2, "会的，小橘生活习惯很好，完全会用猫砂盆。", 2L);
        createComment(cat3, uid4, "雪球的眼睛太美了！布偶猫性格怎么样？");
        createComment(dog1, uid5, "旺财看起来好精神！金毛真的很适合做家庭伴侣犬。");
        createComment(dog1, uid3, "已经申请领养了，希望能通过审核！");
        createComment(dog2, uid5, "豆豆的小短腿太可爱了哈哈哈");

        log.info(">>> 样本数据生成完毕！");
        log.info("    用户: admin/123456, zhangsan/123456, lisi/123456, wangwu/123456, xiaomei/123456");
        log.info("    宠物: {} 只", petMapper.selectCount(null));
        log.info("    申请: {} 条", adoptionMapper.selectCount(null));
        log.info("    收藏: {} 条", favoriteMapper.selectCount(null));
        log.info("    评论: {} 条", commentMapper.selectCount(null));
    }

    private Long createUser(String username, String password, String nickname, String email, String phone, String role) {
        // 检查用户是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)).getId();
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    private Long createPet(String name, Long categoryId, String breed, String age,
                            String gender, String size, String color,
                            String healthStatus, String vaccination, Integer sterilization,
                            String description, String coverImage, String status, Long createdBy) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setCategoryId(categoryId);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setGender(gender);
        pet.setSize(size);
        pet.setColor(color);
        pet.setHealthStatus(healthStatus);
        pet.setVaccination(vaccination);
        pet.setSterilization(sterilization);
        pet.setDescription(description);
        pet.setCoverImage(coverImage);
        pet.setStatus(status);
        pet.setViewCount((int)(Math.random() * 200 + 10));
        pet.setCreatedBy(createdBy);
        petMapper.insert(pet);
        return pet.getId();
    }

    private void addPetImages(Long petId, String... urls) {
        int order = 0;
        for (String url : urls) {
            PetImage img = new PetImage();
            img.setPetId(petId);
            img.setUrl(url);
            img.setSortOrder(order++);
            petImageMapper.insert(img);
        }
    }

    private void createAdoption(Long petId, Long userId, String realName, String idCard,
                                 String phone, String address, String housingType,
                                 Integer hasExperience, Integer familyAgreed,
                                 String occupation, String incomeRange,
                                 String reason, String status, Long reviewedBy) {
        Adoption a = new Adoption();
        a.setPetId(petId);
        a.setUserId(userId);
        a.setRealName(realName);
        a.setIdCard(idCard);
        a.setPhone(phone);
        a.setAddress(address);
        a.setHousingType(housingType);
        a.setHasExperience(hasExperience);
        a.setFamilyAgreed(familyAgreed);
        a.setOccupation(occupation);
        a.setIncomeRange(incomeRange);
        a.setReason(reason);
        a.setStatus(status);
        if ("ADMIN_APPROVED".equals(status) || "APPROVED".equals(status) || "REJECTED".equals(status) || "OWNER_REJECTED".equals(status)) {
            a.setReviewedBy(reviewedBy);
            a.setReviewedAt(java.time.LocalDateTime.now().minusDays((long)(Math.random() * 7 + 1)));
            if ("ADMIN_APPROVED".equals(status)) {
                a.setReviewComment("初审通过，请等待宠物主人最终确认");
            } else if ("APPROVED".equals(status)) {
                a.setReviewComment("管理员初审通过，宠物主人已同意领养");
                a.setOwnerReviewedBy(reviewedBy);
                a.setOwnerReviewedAt(java.time.LocalDateTime.now().minusDays(1));
                a.setOwnerReviewComment("恭喜，请尽快联系我领养");
            } else if ("OWNER_REJECTED".equals(status)) {
                a.setReviewComment("初审已通过");
                a.setOwnerReviewedBy(reviewedBy);
                a.setOwnerReviewedAt(java.time.LocalDateTime.now());
                a.setOwnerReviewComment("抱歉，我觉得不太合适");
            } else {
                a.setReviewComment("抱歉，您目前的居住条件暂不适合领养该宠物");
            }
        }
        adoptionMapper.insert(a);
    }

    private void createFavorite(Long userId, Long petId) {
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setPetId(petId);
        favoriteMapper.insert(f);
    }

    private void createComment(Long petId, Long userId, String content) {
        Comment c = new Comment();
        c.setPetId(petId);
        c.setUserId(userId);
        c.setContent(content);
        commentMapper.insert(c);
    }

    private void createCommentReply(Long petId, Long userId, String content, Long parentId) {
        Comment c = new Comment();
        c.setPetId(petId);
        c.setUserId(userId);
        c.setContent(content);
        c.setParentId(parentId);
        commentMapper.insert(c);
    }
}

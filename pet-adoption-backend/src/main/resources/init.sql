-- 宠物领养平台数据库初始化脚本
-- 数据库: PostgreSQL
-- 执行方式: psql -U postgres -d pet_adoption -f init.sql

-- 创建分类表
CREATE TABLE IF NOT EXISTS t_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 创建宠物信息表
CREATE TABLE IF NOT EXISTS t_pet (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    category_id BIGINT REFERENCES t_category(id),
    breed VARCHAR(50),
    age VARCHAR(20),
    gender VARCHAR(10),
    pet_size VARCHAR(20),
    color VARCHAR(30),
    health_status VARCHAR(100),
    vaccination VARCHAR(100),
    sterilization SMALLINT DEFAULT 0,
    description TEXT,
    cover_image VARCHAR(500),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    view_count INT DEFAULT 0,
    created_by BIGINT REFERENCES t_user(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pet_category ON t_pet(category_id);
CREATE INDEX IF NOT EXISTS idx_pet_status ON t_pet(status);
CREATE INDEX IF NOT EXISTS idx_pet_created_at ON t_pet(created_at);

-- 创建宠物图片表
CREATE TABLE IF NOT EXISTS t_pet_image (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT REFERENCES t_pet(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_pet_image_pet ON t_pet_image(pet_id);

-- 创建领养申请表
CREATE TABLE IF NOT EXISTS t_adoption (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT REFERENCES t_pet(id),
    user_id BIGINT REFERENCES t_user(id),
    real_name VARCHAR(50) NOT NULL,
    id_card VARCHAR(18) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    housing_type VARCHAR(50),
    has_experience SMALLINT DEFAULT 0,
    family_agreed SMALLINT DEFAULT 0,
    occupation VARCHAR(50),
    income_range VARCHAR(30),
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    review_comment TEXT,
    reviewed_by BIGINT REFERENCES t_user(id),
    reviewed_at TIMESTAMP,
    owner_review_comment TEXT,
    owner_reviewed_by BIGINT REFERENCES t_user(id),
    owner_reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_adoption_pet ON t_adoption(pet_id);
CREATE INDEX IF NOT EXISTS idx_adoption_user ON t_adoption(user_id);
CREATE INDEX IF NOT EXISTS idx_adoption_status ON t_adoption(status);

-- 创建收藏表
CREATE TABLE IF NOT EXISTS t_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES t_user(id),
    pet_id BIGINT REFERENCES t_pet(id),
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uk_user_pet UNIQUE (user_id, pet_id)
);

-- 创建评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT REFERENCES t_pet(id),
    user_id BIGINT REFERENCES t_user(id),
    content TEXT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comment_pet ON t_comment(pet_id);

-- 插入默认分类
INSERT INTO t_category (name, description, sort_order) VALUES
    ('猫咪', '各类猫咪待领养', 1),
    ('狗狗', '各类狗狗待领养', 2),
    ('其他', '兔子、仓鼠等其他小动物', 3)
ON CONFLICT DO NOTHING;

-- 默认管理员账号由 DataInitializer 自动创建 (admin / admin123)

#!/bin/bash
# 宠物领养平台 - 一键打包脚本 (Git Bash / Linux / Mac)

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/pet-adoption-frontend"
BACKEND_DIR="$SCRIPT_DIR/pet-adoption-backend"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"
MAVEN_HOME="$BACKEND_DIR/apache-maven-3.9.6"
export JAVA_HOME="${JAVA_HOME:-C:/Program Files/Java/jdk-17.0.2}"

echo "============================================"
echo "  宠物领养平台 - 一键打包脚本"
echo "============================================"
echo ""

# 步骤 1: 构建前端
echo "[1/3] 构建前端..."
cd "$FRONTEND_DIR"
npm run build
echo "✅ 前端构建完成"

# 步骤 2: 复制前端文件到后端 static 目录
echo ""
echo "[2/3] 复制前端文件到后端..."
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r "$FRONTEND_DIR/dist/"* "$STATIC_DIR/"
echo "✅ 复制完成"

# 步骤 3: 构建后端 JAR
echo ""
echo "[3/3] 构建后端 JAR..."
cd "$BACKEND_DIR"
"$MAVEN_HOME/bin/mvn" clean package -DskipTests
echo "✅ 后端构建完成"

echo ""
echo "============================================"
echo "  ✅ 打包完成！"
echo "============================================"
echo ""
echo "  输出文件: pet-adoption-backend/target/pet-adoption-backend-1.0.0.jar"
echo "  运行方式: java -jar pet-adoption-backend/target/pet-adoption-backend-1.0.0.jar"
echo ""
echo "  然后访问: http://localhost:8080"

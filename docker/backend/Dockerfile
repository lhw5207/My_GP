# 后端镜像：只 COPY 宿主机 mvn package 产物，容器内不构建
# jar 通过 compose additional_contexts 的命名上下文 jar: 传入
# （上下文目录 = 本文件所在目录，jar 位于 RuoYi-Vue/ruoyi-admin/target/）
FROM eclipse-temurin:8-jre

ENV TZ=Asia/Shanghai

COPY --from=jar ruoyi-admin.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

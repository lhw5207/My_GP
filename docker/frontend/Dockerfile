# 前端镜像：只 COPY 宿主机 npm run build:prod 产物（dist），容器内不构建
# dist 通过 compose additional_contexts 的命名上下文 dist: 传入
# （主上下文 = 本文件所在目录，含 nginx.conf）
FROM nginx:stable-alpine

ENV TZ=Asia/Shanghai

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=dist . /usr/share/nginx/html

EXPOSE 80

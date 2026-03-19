# Elexvx 项目 1Panel 部署指南

本文档将指导你如何使用 [1Panel](https://1panel.cn/) 面板部署 Elexvx 前后端分离项目。

## 环境准备

在开始部署前，请确保你的 1Panel 面板已安装以下应用（在“应用商店”中安装）：

1.  **OpenResty** (用于前端静态资源托管和反向代理)
2.  **MySQL** (推荐 5.7 或 8.0)
3.  **Redis** (最新版即可)
4.  **Java 环境** (如果选择在服务器编译或运行 Jar 包，建议安装 JDK 17 或以上，可在“主机”->“环境”中管理，或直接使用 Docker 部署)

---

## 1. 数据库准备

1.  进入 1Panel 面板 -> **数据库** -> **MySQL**。
2.  点击 **创建数据库**：
    *   名称：`tdesign` (或自定义)
    *   用户：`tdesign` (或自定义)
    *   权限：读写
3.  进入数据库管理页面 (phpMyAdmin 或 1Panel 自带的 SQL 导入功能)。
4.  依次导入项目根目录下的 SQL 脚本：
    1.  `database/tdesign_init.sql` (MySQL 初始化结构和数据)
    2.  `database/tdesign_migration_v2.sql` (增量更新，如有)

---

## 2. 后端部署 (Spring Boot)

### 方案 A：Jar 包运行 (推荐)

1.  **本地编译**：
    在本地开发环境中，进入 `backend` 目录，执行 Maven 打包命令：
    ```bash
    cd backend
    mvn clean package -DskipTests
    ```
    打包完成后，在 `backend/target` 目录下会生成一个 `.jar` 文件 (例如 `elexvx-backend-0.0.1-SNAPSHOT.jar`)。

2.  **上传文件**：
    在 1Panel -> **文件** 中，创建一个目录 `/opt/1panel/apps/tdesign/backend` (示例路径)，将 Jar 包上传上去。

3.  **配置修改**：
    确保 Jar 包同级目录下有一个 `application.yml` 配置文件（可以从源码 `backend/src/main/resources/application.yml` 复制一份）。
    修改 `application.yml` 中的数据库和 Redis 连接信息：
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://127.0.0.1:3306/tdesign?useUnicode=true&characterEncoding=utf-8&useSSL=false
        username: tdesign  # 你的数据库用户名
        password: your_password # 你的数据库密码
      data:
        redis:
          host: 127.0.0.1
          port: 6379
          password: your_redis_password # 如有密码
    ```

4.  **创建运行环境**：
    在 1Panel -> **网站** -> **运行环境** (或使用 **进程守护 / Supervisor**)：
    *   **名称**：Elexvx-Backend
    *   **运行目录**：`/opt/1panel/apps/tdesign/backend`
    *   **启动命令**：
        ```bash
        java -jar elexvx-backend-0.0.1-SNAPSHOT.jar
        ```
    *   **端口**：9999 (默认)

    点击确认启动，并查看日志确保启动成功。

---

## 3. 前端部署 (Vue 3)

1.  **本地编译**：
    在本地 `frontend` 目录下执行：
    ```bash
    cd frontend
    npm install
    npm run build
    ```
    构建完成后，会生成一个 `dist` 目录。
    
    注意：前端接口地址是“打包时写死”的。自建部署请确保使用 `frontend/.env.release`（`npm run build` 默认 `--mode release`），并设置：
    * `VITE_IS_REQUEST_PROXY = false`（推荐，走同域 `/api` 反代）
    * `VITE_API_URL` 置空
    否则可能会把接口请求打到示例网关地址，导致接口 404。

2.  **创建网站**：
    在 1Panel -> **网站** -> **OpenResty**，点击 **创建网站**：
    *   **类型**：静态网站
    *   **主域名**：你的域名 (或服务器 IP:端口，如 `127.0.0.1:8888`)
    *   **代号**：elexvx-frontend

3.  **上传静态文件**：
    进入网站目录 (通常是 `/opt/1panel/apps/openresty/www/sites/elexvx-frontend/index`)。
    删除原有文件，将本地 `frontend/dist` 目录下的**所有内容**上传到该目录。

4.  **配置反向代理 (关键)**：
    在网站设置 -> **反向代理** 中，添加一条规则：
    *   **名称**：API 代理
    *   **路径**：`/api`
    *   **代理地址**：`http://127.0.0.1:9999` (后端服务的地址)

    *或者直接修改 Nginx 配置文件 (网站设置 -> 配置文件)*：
    ```nginx
    location /api {
        proxy_pass http://127.0.0.1:9999;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 防止 Vue 路由 404
    location / {
        try_files $uri $uri/ /index.html;
    }
    ```

---

## 4. 验证与访问

1.  确保后端服务已启动且日志无报错。
2.  确保前端网站已运行。
3.  在浏览器访问你配置的前端域名或 IP。
4.  尝试登录 (默认账号通常是 admin / 123456，具体请查看数据库初始化脚本)。

## 常见问题

*   **跨域问题**：通过 Nginx 反向代理 `/api` 可以完美解决跨域问题，请确保代理配置正确。
*   **数据库连接失败**：请检查 Docker 容器间的网络互通性。如果使用 `127.0.0.1` 无法连接，尝试使用 1Panel 提供的数据库容器 IP 或服务名。
*   **前端 404**：刷新页面报 404 是 SPA 应用的通病，请务必在 Nginx 配置中添加 `try_files $uri $uri/ /index.html;`。

## 4. 生产安全必填环境变量

请在后端容器/应用中至少配置以下变量：

- `SPRING_PROFILES_ACTIVE=prod`
- `ELEXVX_SECURITY_FIELD_SECRET=<32bytes以上随机密钥>`
- `ELEXVX_CORS_ALLOWED_ORIGINS=https://finance.example.com,https://admin.example.com`
- `FILE_TOKEN_SECRET=<高强度随机密钥>`
- （可选）`ELEXVX_FILE_TOKEN_TTL_SECONDS=600`
- （可选）`ELEXVX_UPLOAD_MAX_FILE_SIZE_MB=100`

未配置 `ELEXVX_SECURITY_FIELD_SECRET` 或在 `prod` 下未配置 CORS 白名单时，服务会启动失败（Fail Fast）。

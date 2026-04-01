# 数据范围管理演示版

这个仓库包含一个可直接运行的前后端分离演示项目：

- `backend/`：Spring Boot + H2 内存数据库
- `frontend/`：Vue 2 + ElementUI

## 运行方式

### 1. 启动后端

```bash
cd backend
../mvnw spring-boot:run
```

后端默认启动在 `http://localhost:8080`。

如果本机对默认 Maven 缓存目录没有写权限，可以改用临时目录：

```bash
cd backend
../mvnw -Dmaven.repo.local=/tmp/pipm-selectRange-m2 spring-boot:run
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run serve
```

前端默认启动在 `http://localhost:8081`。

如果本机对默认 npm 缓存目录没有写权限，可以改用临时目录：

```bash
cd frontend
npm install --cache /tmp/pipm-selectRange-npm-cache
npm run serve
```

## 演示说明

- 顶部可切换演示用户，前端会通过 `x-demo-user-id` 请求头传给后端
- 演示查询页固定使用 `pageCode=DEMO_REPORT_QUERY`
- 可在“页面白名单管理 / 默认规则管理 / 个人覆盖管理”中实时调整规则
- 返回查询页重新查询即可看到数据范围变化

## 技术说明

- 后端无需额外数据库配置，启动时会自动执行 `schema.sql` 和 `data.sql`
- 前端通过 devServer 代理 `/api` 到后端
- 仓库根目录的 `mvnw` 会在本地缺少 Maven 时自动下载 Maven 3.9.9

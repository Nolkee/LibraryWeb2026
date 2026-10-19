# LibraryWeb2026 知阅图书馆

Java Web 作业2。使用 Maven 创建 WAR 项目，采用 Servlet + JDBC + MySQL，同一个 WAR 同时提供静态前端和后端接口；不使用 JSP、Spring Boot、MyBatis 或独立前端服务。

## 技术与结构

- JDK 17、Tomcat 10.1、Jakarta Servlet 6、MySQL、Gson。
- HTML / CSS / jQuery 3.7.1 / Axios 1.7.9 / Vue 3。前端依赖放在 `assets/`，运行时无需 CDN。
- `edu.homework.jdbc`：直接复用作业1的注解及 JDBCTool。
- `edu.library.model`：Base、Book、BookCopy、抽象 User、Admin、Operator、Student、Teacher、Loan，包含 getter/setter 和角色权限。
- `DB`：JDBC连接、参数绑定及查询；`ApiServlet`：处理浏览器请求、会话鉴权和业务事务。
- 用户按角色通过 JDBCTool.getOneById 查询；图书、副本和借阅通过 JDBCTool.save 存储，跨表检索和原子更新使用预编译 JDBC 语句。

## 已实现功能

| 角色 | 功能 |
| --- | --- |
| 全部角色 | 注册、验证码登录、退出、首页角色卡片、借书、还书 |
| 学生 | 全部/未还/超期/日期范围个人借阅查询 |
| 教师 | 个人借阅、推荐图书、查看审核状态及驳回理由 |
| 操作员 | 图书列表与模糊查询、新增/编辑/废弃、图书副本新增/删除、个人借阅 |
| 管理员 | 只读用户模糊查询及详情弹窗、全馆借阅关键字/超期/日期范围查询、按类型/专业统计、推荐审核入馆或驳回 |

注册要求姓名和手机号不重复，手机号验证为中国大陆11位号码。角色选择后动态显示对应信息。新密码使用 PBKDF2 加盐哈希；兼容课程备份中已有的明文密码。验证码保存在服务端会话中，五分钟有效且一次性使用。服务端独立检查权限，不依赖前端隐藏按钮。

图书新增与副本生成、借还书、库存变更及审核入馆均在事务中完成。借书与归还使用图书行锁，避免同一副本重复借出。归还仅能操作当前登录用户的未还副本。副本外借时不能删除，存在外借副本时不能废弃图书。

## 数据库来自附件备份

使用教师提供并解压后的 `../LibraryDB/`。其中 `.sql.gz` 是 Navicat 记录分隔格式，不能直接作为完整 INSERT 脚本执行。转换脚本从元数据读取真实建表语句及列顺序，恢复原始数据，不另造一套示例数据库。

恢复后原始业务数据为：54本图书、167个副本、17个用户、15条借阅记录。备份部分记录的库存与副本总数不一致，本项目保留原始数据；可借数量独立按实际未借出的有效副本统计。录屏新增的示例记录会增加上述数量。

恢复后执行 `scripts/migration.sql`，只为 bookcopy 增加可空的 `deletedAt` 字段：删除在馆副本时标记移除、库存减1，馆藏副本列表与借阅候选排除该副本，历史 loan 外键仍保留。这样既能删除已有历史借阅的副本，又不破坏历史记录。废弃图书只修改状态，不删除借阅数据。

## 本机启动

当前机器已经准备了 `.runtime/` 内独立 MySQL 数据目录与 Tomcat。MySQL 使用127.0.0.1:3307，不接管机器原有的3306数据目录。Tomcat 使用8080。

```bash
cd '/Users/lucas/CJU/class/Java应用开发框架/javaweb/LibraryWeb2026'
bash scripts/start-local.sh
```

`start-local.sh` 自动启动本机实例，构建并部署 WAR。首次在本机新建实例且数据库尚未恢复时，启动后执行一次：

```bash
bash scripts/restore-local.sh ../LibraryDB
```

已存在同名数据库时恢复脚本拒绝覆盖。`.runtime/` 不入 Git，其中包含机器专用服务和恢复的原始数据。

访问 `http://localhost:8080/LibraryWeb2026/`。本机当前局域网地址见 `docs/submission.md`；更换网络后运行 `ipconfig getifaddr en0` 取得新地址。其他设备需连接同一局域网，浏览器访问 `http://本机IP:8080/LibraryWeb2026/`。校园网络若启用客户端隔离，需要改用允许设备互访的路由器或热点。

`.zshrc` 已追加 JDK17 的 JAVA_HOME、Tomcat 的 CATALINA_HOME / CATALINA_BASE 和 PATH。新终端自动生效。脚本内部的环境配置位于 `scripts/env.sh`。

## 换电脑部署

安装 JDK17、Maven、MySQL 和 Tomcat10.1；从附件恢复数据并执行上述迁移，然后设置：

```bash
export JAVA_HOME='/path/to/jdk17'
export CATALINA_HOME='/path/to/apache-tomcat-10.1'
export LIBRARY_DB_URL='jdbc:mysql://127.0.0.1:3306/LibraryWeb2026?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai'
export LIBRARY_DB_USER='your_mysql_user'
export LIBRARY_DB_PASSWORD='your_mysql_password'
mvn clean package
cp target/LibraryWeb2026.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/startup.sh"
```

MySQL账号应有该库的读写权限。Windows将对应值设置到系统环境变量，使用 `startup.bat` 启动。

## 课程备份内测试账号

| 角色 | 姓名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 操作员 | operator | operator123 |
| 学生 | student1 | student123 |
| 教师 | teacher | teacher123 |

这些是附件中的课程测试账号，仅用于作业演示。项目不包含个人账号凭据和原始数据库备份。

## 验证与交付

```bash
mvn test package
python3 scripts/integration_test.py
```

集成验证连接本机 Tomcat 与恢复后的 MySQL，覆盖63项 HTTP 检查；临时数据使用随机前缀，最后只清理自己创建的记录。该脚本的数据库清理参数针对本机3307；换环境时同步调整。

WAR：`target/LibraryWeb2026.war`。录屏脚本与时间轴说明位于 `scripts/record_demo.py`、`docs/`。仓库及视频交付状态见 `docs/submission.md`。

## 依赖许可

jQuery、Axios、Vue 为第三方开源依赖；各自许可证声明保留在分发文件头部。MySQL驱动、Servlet API及Gson由Maven管理。作业1工具代码来源于同目录的 jdbc-tool-homework 项目。

## 代码仓库

- GitHub（公开）：https://github.com/Nolkee/LibraryWeb2026
- Gitee（当前私有）：https://gitee.com/nolokke/LibraryWeb2026

本地已配置 origin 与 gitee 两个 remote。后续每完成一个功能，提交后分别推送：

```bash
git add .
git commit -m "feat: 本次完成的功能"
git push origin main
git push gitee main
```

Gitee命令行推送需要自行配置该账号的Git认证。本次在账号登录后通过网页导入已有GitHub提交历史；导入保留提交，不代表在此前每完成一个功能时已经分别推送至Gitee。

# LibraryWeb2026 图书馆管理系统

这是 Java Web 课程作业2，用 Maven 创建项目，实现图书馆的用户管理、图书管理和借还书功能。

前端使用 HTML、CSS、jQuery、Axios 和 Vue，后端使用 Servlet + JDBC，数据库使用 MySQL。数据库从老师提供的 LibraryDB 备份恢复，数据库操作复用了作业1的 JDBCTool。项目不使用 JSP。

## 主要功能

- 注册、登录、验证码和退出登录。
- 根据管理员、操作员、学生、教师显示不同的功能。
- 添加、查询、编辑和废弃图书，管理图书副本。
- 借书、还书，查看个人借阅记录。
- 教师推荐图书，管理员审核推荐。
- 管理员查询用户和借阅记录，统计借阅次数。

添加图书时会按库存数量生成副本。借出的副本不能删除，有图书没归还时不能废弃。图书废弃后，原来的借阅记录仍然保留。

## 运行方法

需要 JDK 17、Maven、MySQL 和 Tomcat 10.1。

在这台电脑上，进入项目文件夹后运行：

```bash
bash scripts/start-local.sh
```

然后打开 [图书馆首页](http://localhost:8080/LibraryWeb2026/)。

如果数据库还没恢复，先启动服务，再执行一次：

```bash
bash scripts/restore-local.sh ../LibraryDB
```

本机 MySQL 使用3307端口，Tomcat使用8080端口。数据库设置在 `scripts/env.sh` 中，换电脑时需要按实际安装位置修改。

## 测试账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 操作员 | operator | operator123 |
| 学生 | student1 | student123 |
| 教师 | teacher | teacher123 |

这些账号来自老师提供的数据库，可以直接登录测试。

## 打包和局域网访问

运行下面的命令打包：

```bash
mvn package
```

生成的文件为 `target/LibraryWeb2026.war`，放到 Tomcat 的 `webapps` 文件夹中即可运行。

同一局域网中的其他设备可以访问 `http://电脑IP:8080/LibraryWeb2026/`。

## 代码仓库

- [Gitee](https://gitee.com/nolokke/LibraryWeb2026)（目前为私有仓库）
- [GitHub](https://github.com/Nolkee/LibraryWeb2026)（公开）

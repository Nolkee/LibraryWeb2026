# 作业2 提交材料

项目名：LibraryWeb2026。

技术：Maven + HTML/CSS/jQuery/Axios/Vue + Servlet + JDBC + MySQL，同一 WAR 部署至 Tomcat10.1，不使用 JSP，不采用前后端分离。

完成范围：附件规定的用户功能、图书功能，以及选做的借阅查询和统计功能。数据库通过附件 LibraryDB 备份恢复，使用已有馆藏和账号测试。

- GitHub仓库：[Nolkee/LibraryWeb2026](https://github.com/Nolkee/LibraryWeb2026)，已创建并推送。
- Gitee仓库：[nolokke/LibraryWeb2026](https://gitee.com/nolokke/LibraryWeb2026)，当前为私有仓库；新账号公开发布需先完成平台要求的安全验证。通过Gitee网页从GitHub导入完整提交历史，已核对main分支与全部6次原有提交。
- WAR：`target/LibraryWeb2026.war`。
- 视频：`target/LibraryWeb2026-演示视频.mp4`，94秒，H.264，1414×786，已生成并抽查画面；尚未上传课程答案区。
- 本机地址：http://localhost:8080/LibraryWeb2026/
- 当前局域网地址：http://10.161.56.246:8080/LibraryWeb2026/

已在本机通过该局域网 IP 访问；尚未使用第二台设备验证跨设备连通性，不能将本机自访问等同于第二台设备测试。

本机 JAVA_HOME、CATALINA_HOME、CATALINA_BASE、PATH 已配置。原有 MySQL 缺少升级前的动态依赖，本项目使用保留的匹配库启动独立3307实例，未修改原有数据库。

## 提交前检查

1. GitHub已公开；Gitee为私有，若老师需要访问Gitee，请先完成平台安全验证并开放仓库，或授予老师访问权限。
2. 本地已生成 WAR、源码ZIP及演示视频，可从 target 目录取用。
3. 用同一网络的手机/电脑打开局域网地址；如校园网隔离设备，可使用手机热点。
4. 在课程答案区填写 GitHub 地址，上传演示视频，检查上传完成后再提交。

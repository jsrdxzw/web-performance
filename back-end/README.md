## java后端性能优化方案

后端优化分为代码层面的优化，数据库优化，并发优化，工具类合理选用等等

### demo示例说明
本次采用的是基于spring boot框架开发的咖啡店预定系统，其中主要使用了以下的技术：
+ MQ消息中间件
+ Redis缓存技术

### 工具类选用及配置
本次项目使用的是spring boot`2.x`版本，其中集成了了当前性能最好的第三方扩展库,
配置文件如application.yml所示。
+ 数据库连接池：hikariCP(spring 2.x默认)
+ Mysql配置优化，充分利用缓存查询，与hikariCP搭配使用性能最好
+ 使用了Mybatis以方便SQL更细粒化的控制

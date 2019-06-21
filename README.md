## web 性能的提升方案

web性能包含了很多方面，这个项目是根据前人的经验和自己的思考，对前端和后端
web性能的提升做了一个总结

### 前端部分(Html+Css+Js)
未使用框架，采用webpack优化前端的静态资源

### 后端部分(Java)
后端部分主要涉及了以下部分：
+ jvm优化
+ 数据库优化
+ 设计模式优化
+ 代码优化
+ 并发优化

### Nginx部分
主要配置了缓存设置和常用优化配置，如设置缓存生效日期，多核并发处理，防盗链设置等等

缓存的工作流程如下图:

![浏览器处理缓存策略图](https://user-gold-cdn.xitu.io/2018/5/28/163a4d01fdd197b6?imageView2/0/w/1280/h/960/ignore-error/1)


### 附录部分
这里参考了web请求过程的示意图:

![web请求过程示意图](https://user-gold-cdn.xitu.io/2018/5/28/163a4d01fdc524f3?imageView2/0/w/1280/h/960/ignore-error/1)
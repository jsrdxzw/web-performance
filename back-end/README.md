## java后端性能优化方案

后端优化分为代码层面的优化，数据库优化，并发优化，工具类合理选用等等

### 接口性能测试方法
在linux环境中可以使用`httpd-tools`进行压力模拟测试
+ 安装：yum -y install httpd-tools
+ 使用方法：ab -n 100000(请求数) -c 1000(并发用户数)
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

### 代码层面的优化

#### 谨慎使用正则表达式
正则表达式默认是贪婪匹配，比如`\ab${1,3}c\`就是尽可能的匹配多的b，
这样会造成回溯的问题，占用CPU的计算资源。

我们可以使用懒惰匹配`?`以匹配尽可能少的内容，比如`\ab${1,3}?c\`,
表示匹配了一个b之后就不会继续往下匹配。或者采用独占匹配`+`以匹配尽可能多的内容，
比如`\ab${1,3}+bc\`表示如果要匹配`abbc`的话，结果是不匹配的，因为`b${1,3}+`
已经将b匹配完了，后面的c不等于b，匹配不成功。

在分组匹配的时候，我们一般只想取出一部分分组的内容，这时候可以使用`(?:xxx)`
来优化分组匹配计算
```java
class Example{
   public static void main(String[] args){
     String text = "<input>test</input>";
     String reg = "(?:<input.*?>)(.*?)(?:</input>)";
     Pattern p = Pattern.compile(reg);
     Matcher m = p.matcher(text);
     while (m.find()){
         System.out.println(m.group(0)); // 所有匹配到的内容
         System.out.println(m.group(1)); //(.*?) 即 test
     }
   } 
}
```
另外，尽量使用String的indexOf方法替代正则表达式

#### 正确使用List数据结构
常用的List数据结构分为ArrayList和LinkedList，
其中ArrayList基于数组实现的，支持动态扩容和快速随机访问，
而LinkedList则基于双向链表实现。

*我们在插入或者删除头元素的时候使用LinkedList，否则其他情况尽可能使用ArrayList*，
在遍历ArrayList的时候，使用for循环比forEach循环更快（数组下标访问是最快的）
```java
class Example{
    public static void main(String[] args){
        List<String> array = new ArrayList<>();
        array.add("1");
        array.add("2");
        array.add("3");
        for(int i = 0; i < array.size(); i++) {
          System.out.println(i);
        }
    }
}
```
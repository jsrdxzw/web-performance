## java后端性能优化方案

后端优化分为代码层面的优化，数据库优化，并发优化，工具类合理选用等等

### 接口性能测试方法
#### 使用`httpd-tools`进行压力模拟测试
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

### 升级java
java11相比较于java8有16%以上的性能突破，
并且具有更高效的垃圾回收算法`ZGC`,建议升级，
具体在intelliJ IDEA上的操作可参见于[Jdk8到jdk11 SpringBoot 踩坑指南](https://sargeraswang.com/blog/2018/11/10/jdk8dao-jdk11-springboot-cai-keng-zhi-nan/)

### 代码层面的优化

#### 使用javap反编译代码，查看java字节码分析
在分析java性能时候，我们需要使用javap的方式读字节码来分析问题，
比如在intelliJ IDEA中，我们可以配置javap的运行方式，动态查看java类的编译结果，具体配置如下：

![javap](assets/javap.png)

然后在java文件中右键选择external tools的javap即可。


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

我们知道基于链表的数据结构，在查找的时候都得先遍历一遍，
但是插入数据的时候一定比ArrayList快吗？
网上有很多资料说得也不准确，在插入数据的时候得具体情况分析：
+ 在尾部插入数据，ArrayList不需要做数据移动，LinkedList需要新建节点对象，总体ArrayList快于LinkedList
+ 在头部插入数据，LinkedList快于ArrayList
+ 在中间插入数据，LinkedList需要遍历，算法复杂度为O(n)，ArrayList也需要移动数组数据，但是ArrayList只需要直接覆盖值操作，LinkedList
  需要创建对象，改变指针等操作，所以在时间上ArrayList快于LinkedList
  
删除操作也是同理

**我们在插入或者删除头元素的时候使用LinkedList，否则其他情况尽可能使用ArrayList**，

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

#### 字符串操作
字符串拼接操作请务必使用`StringBuilder`，有并发的情况使用`StringBuffer`，
此外在创建字符串的时候禁止使用new创建
```java
public class Example{
    public static void main(String[] args){
      String str = new String("qwe"); // not good
      String str = "qwe"; // good
    }
}
```

#### 借助Stream操作优化遍历，提升性能
首先看一个按照性别过滤身高小于160的学生的例子，传统的做法如下所示：
```java
class Example{
    public static void main(String[] args){
       Map<String, List<Student>> stuMap = new HashMap<>();
              List<Student> studentList = new ArrayList<>();
              studentList.add(new Student(150, "man"));
              studentList.add(new Student(160, "woman"));
              studentList.add(new Student(170, "man"));
              studentList.add(new Student(165, "woman"));
              for (Student student : studentList) {
                  if (student.getHeight() > 160) {
                      if (stuMap.get(student.getSex()) == null) {
                          List<Student> list = new ArrayList<>();
                          list.add(student);
                          stuMap.put(student.getSex(), list);
                      } else {
                          stuMap.get(student.getSex()).add(student);
                      }
                  }
              }
              System.out.println(stuMap); // {man=[Student{height=170, sex='man'}], woman=[Student{height=165, sex='woman'}]}
    }
}
```
如果我们使用Stream的方式处理数据，代码不但变得简单，在处理大量数据的时候，也会提高性能：
```java
public class Example {
    public static void main(String[] args) {
        Map<String, List<Student>> stuMap;
        List<Student> studentList = new ArrayList<>();
        studentList.add(new Student(150, "man"));
        studentList.add(new Student(160, "woman"));
        studentList.add(new Student(170, "man"));
        studentList.add(new Student(165, "woman"));
        // 使用串行方式
        stuMap = studentList.stream().filter(s -> s.getHeight() > 160).collect(Collectors.groupingBy(Student::getSex));
        System.out.println(stuMap); // {man=[Student{height=170, sex='man'}], woman=[Student{height=165, sex='woman'}]}
        // 使用并行方式
        stuMap = studentList.parallelStream().filter(s -> s.getHeight() > 160).collect(Collectors.groupingBy(Student::getSex));
        System.out.println(stuMap); // {man=[Student{height=170, sex='man'}], woman=[Student{height=165, sex='woman'}]}
    }
}
```
再看一个例子
```java
class Example{
    public static void main(String[] args){
      List<String> names = Arrays.asList("张三", "李四", "王老五", "李三", "刘老四", "王小二", "张四", "张五六七");
      // 并行处理
      int res = names.stream().parallel().filter(name -> name.startsWith("张")).mapToInt(String::length).max().orElse(0);
      System.out.println(res); // 4
    }
}
```
总结一下Stream的使用：
+ 在循环迭代次数较少的情况下，常规的迭代方式性能反而更好
+ 在单核 CPU 服务器配置环境中，也是常规迭代方式更有优势
+ 数据量大，如果服务器是多核 CPU 的情况下，Stream 的并行迭代**优势明显**，注意并行计算后终止操作为Collect
+ 并行操作时，需要考虑线程安全的问题


#### HashMap的使用
HashMap在使用上强烈建议设置初始值size，计算公式为`dataSize/LOAD_FACTOR`
其中dataSize为要存储的值大小，load_factor为hashMap的装载因子，默认为0.75

#### 合理使用try-catch
try-catch在java程序中对性能是有影响的，特别是在循环语句中：

```java
// bad code
int a = 0;
for(int i=0;i<100000000;i++){
    try {
        a++;
    } catch (Exception e){}
}

// good code
int a = 0;
try {
    for(int i=0;i<100000000;i++){
        a++;
    }
} catch (Exception e){}
```
后面的代码比前面的代码性能至少提升两倍

#### 工具类务必使用static静态方法实现
静态方法的调用速度快于实例方法

#### 使用`arrayCopy`进行数组复制
在数据复制的时候，我们可以通过`arrayCopy`对原数组进行复制，
这样能极大提高复制性能。

#### 务必使用nio，不要使用传统的io操作
IO数据流操作，比如网络数据传输，文件读写等等耗时操作，很容易成为系统
性能的瓶颈，nio下的文件流操作大大快于传统的io操作。

nio文件的创建，内容读写操作例子：
```java
public class SimpleFile {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("mystuff.txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        try (
                FileChannel writeChannel = new FileOutputStream("mystuff.txt").getChannel();
                FileChannel readChannel = new FileInputStream("mystuff.txt").getChannel()
        ) {
            // 创建读写字节缓冲
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put("I love you".getBytes());
            writeBuffer.flip();
            writeChannel.write(writeBuffer);


            readBuffer.clear();
            readChannel.read(readBuffer);
            readBuffer.flip();
            StringBuilder stringBuilder = new StringBuilder();
            while (readBuffer.hasRemaining()) {
                stringBuilder.append((char) readBuffer.get());
            }
            System.out.println("文件的内容：" + stringBuilder);
        }
    }
}
```

如果我们要读写大文件，则需要使用内存映射，它能够创建和修改那些大到无法读入内存的文件。
有了内存映射文件，你就可以认为文件已经全部读进了内存，然后把它当成一个非常大的数组来访问了。
内存映射的读写速度非常快，下面是使用内存映射读写文件的例子：

```java
public class LargeMappedFiles {
  static int length = 0x8000000; // 128 MB
  public static void
  main(String[] args) throws Exception {
    try(
      RandomAccessFile tdat = new RandomAccessFile("test.dat", "rw")
    ) {
      MappedByteBuffer out = tdat.getChannel().map(
        FileChannel.MapMode.READ_WRITE, 0, length);
      for(int i = 0; i < length; i++)
          // 开始写入文件
        out.put((byte)'x');
      System.out.println("Finished writing");
      for(int i = length/2; i < length/2 + 6; i++)
        System.out.print((char)out.get(i));
    }
  }
}
```
输出结果:
```bash
Finished writing
xxxxxx
```
注意这里的`MappedByteBuffer`拥有`ByteBuffer`的所有方法。

最后一个概念是直接缓冲区，它是以创建和销毁时的开销换取了IO操作的高效率。
另外一点是，直接缓冲区使用的内存是直接调用了操作系统api分配的，绕过了JVM堆栈。 
直接缓冲区通过ByteBuffer.allocateDirect()方法创建，并可以调用isDirect()来查询一个缓冲区是否为直接缓冲区。 
一般来说，如果需要频繁的读写操作，直接缓冲区是性能最高的IO选择。

### Tomcat 层面的优化
#### Tomcat性能测试
使用apache jmeter测试工具进行Tomcat性能测试。
jmeter可以模拟用户并发请求，主要测试步骤如下图所示：

1. 保存测试用例 
![1537952165502](assets/1537952165502.png)

2. 添加线程组，使用线程模拟用户的并发

![1537968506080](assets/1537968506080.png)

1000个线程，每个线程循环10次，也就是tomcat会接收到10000个请求。

3. 添加http请求

![1537970361030](assets/1537970361030.png)

![1537970437128](assets/1537970437128.png)

4. 添加请求监控

![1537970530920](assets/1537970530920.png)

5. 启动、进行测试

6. 聚合报告
在聚合报告中，重点看吞吐量。
![1538727715123](assets/1538727715123.png)

测试可以看出，tomcat在不做任何调整时，吞吐量为73次/秒。

#### 配置Tomcat
在调优Tomcat之前，需要配置Tomcat允许本地连接，
在tomcat-users.xml文件中，添加以下内容，增加tomcat的用户访问控制

```html
<role rolename="manager"/>
<role rolename="manager-gui"/>
<role rolename="admin"/>
<role rolename="admin-gui"/>
<user username="tomcat" password="tomcat" roles="admin-gui,admin,manager-gui,manager"/>
```
如果是7以上的版本，并且希望远程主机登录的话，还需要修改`webapps/manager/META-INF/context.xml`
```html
<Context antiResourceLocking="false" privileged="true" >
 <!-- 这段需要注释掉-->
 <!-- <Valve className="org.apache.catalina.valves.RemoteAddrValve"
         allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" /> -->
  <Manager sessionAttributeValueClassNameFilter="java\.lang\.(?:Boolean|Integer|Long|Number|String)|org\.apache\.catalina\.filters\.CsrfPreventionFilter\$LruCache(?:\$1)?|java\.util\.(?:Linked)?HashMap"/>
</Context>
```
通过以上的配置，我们就可以通过tomcat的server status来查看JVM的运行状态，从而进行调优

#### 禁用AJP
一般的大型项目采用的都是 Nginx+Tomcat，我们就不需要AJP了(默认8009端口),节省资源，
在`server.xml`文件中直接禁用掉即可。

```html
<!-- Define an AJP 1.3 Connector on port 8009 -->
<!--    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />-->
```
然后重新启动Tomcat服务器

#### 优化Tomcat线程池
Tomcat中为每一个http请求都会创建一个线程，我们可以通过设置线程池来提高性能，
修改`server.xml`文件，打开线程池的注释，并在Connector中指定线程池

```html
    <!--The connectors can use a shared executor, you can define one or more named thread pools-->

    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
        maxThreads="500" minSpareThreads="50" prestartminSpareThreads="true" maxQueueSize="100"/>
        
    <!--
    参数说明：
    maxThreads：最大并发数，默认设置 200，一般建议在 500 ~ 1000，根据硬件设施和业务来判断
    minSpareThreads：Tomcat 初始化时创建的线程数
    prestartminSpareThreads： 在 Tomcat 初始化的时候就初始化 minSpareThreads 的参数值，如果不等于 true，minSpareThreads 的值就没啥效果了
    maxQueueSize，最大的等待队列数，超过则拒绝请求，保证服务器的安全，防止雪崩效应，
    并且可以加快平均响应时间，典型12306
    -->
    
    <!--    这里需要指定executor-->
    <Connector executor="tomcatThreadPool"
               port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
```
*需要注意的是，在tomcat status中，max-threads的值显示的是-1，这个是表示配置已经生效的意思*

#### Tomcat运行模式
tomcat的运行模式有3种：
1. bio
   默认的模式,性能非常低下,没有经过任何优化处理和支持.
2. nio
   nio(new I/O)，是Java SE 1.4及后续版本提供的一种新的I/O操作方式(即java.nio包及其子包)。
   Java nio是一个基于缓冲区、并能提供非阻塞I/O操作的Java API，因此nio也被看成是non-blocking I/O的缩写。它拥有比传统I/O操作(bio)更好的并发运行性能。
3. apr
   安装起来最困难,但是从操作系统级别来解决异步的IO问题,大幅度的提高性能.

推荐使用nio，不过，在tomcat8中有最新的nio2，速度更快，建议使用nio2。
如果对应用的性能有极致化的要求，推荐使用apr。

**开启nio2运行模式:**

仍然是在`server.xml`文件中进行修改
```html
<Connector executor="tomcatThreadPool"  port="8080" protocol="org.apache.coyote.http11.Http11Nio2Protocol"
               connectionTimeout="20000"
               redirectPort="8443" />
```

#### 调整JVM参数

GC的次数减少，意味着性能的提高，我们可以通过`easyGC`来查看GC的次数，GC时间等关键参数。

*设置G1垃圾回收器*

我们可以设置G1垃圾回收器来查看是否能够提高Java程序的性能，在`catalina.sh`中上面的注释后添加jvm运行参数：
```bash
# GC最大停顿时间100毫秒，初始堆内存128m，最大堆内存1024m,具体按照服务器的实际情况设置
# 最大堆大小设置应因此至少比应用程序的最大占用率高出 43%。
# 例如，如果应用程序的最大占用率为 70 MB，那么应按照以下计算所示设置 100 MB 的最大堆大小：
# 70 + (70 * 43/100)
JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Xms128m -Xmx1024m -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:../logs/gc.log"
```

*设置ZGC垃圾回收器*

ZGC是java11 新增的垃圾回收算法，暂停时间不会超过10毫秒，
在java11中默认的是G1垃圾回收算法，我们可以手动设置ZGC

```bash
JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Xmx2048m -Xlog:gc"
```

总结上面堆内存的分配：
1. 具体根据每个应用的情况来，比如日均请求量、fullgc之后内存的大小均值等(+43%)
2. 建议最大不超过操作系统内存的3/4

然后通过jmeter进行性能测试，并且通过`easyGC`来查看程序的GC情况，
一般需要经过多次参数调优，才能得到理想的结果。

### 数据库优化
#### 索引建立规范
1. 出现在SELECT、UPDATE、DELETE语句的WHERE从句中的列
2. 包含在ORDER BY、GROUP BY、DISTINCT中的字段
   并不要将符合1和2中的字段的列都建立一个索引， 通常将1、2中的字段建立联合索引效果更好, 联合索引遵循最左匹配原则(B+树)。
3. 多表join的关联列(不建议使用外键约束（foreign key），但一定要在表与表之间的关联键上建立索引,外键可用于保证数据的参照完整性，但建议在业务端实现
                                                         外键会影响父表和子表的写操作从而降低性能)
    
4. 不要在一个字段上建立重复索引，如:
    ```sql
        --  重复索引示例：
        primary key(id)、index(id)、unique index(id)
    ```
#### 索引建立顺序
1. 区分度最高的放在联合索引的最左侧（区分度=列中不同值的数量/列的总行数）
2. 尽量把字段长度小的列放在联合索引的最左侧（因为字段长度越小，一页能存储的数据量越大，IO性能也就越好）
3. 使用最频繁的列放到联合索引的左侧（这样可以比较少的建立一些索引）
4. 索引列不能有**null**，这点切记
#### sql开发规范
1. 使用编程语言的预编译语句进行数据库操作（Prepared Statements）
2. 避免使用双%号的查询条件
3. 使用left join 或 not exists 来优化not in 操作
4. 禁止使用SELECT * 必须使用SELECT <字段列表> 查询
5. 禁止使用不含字段列表的INSERT语句
    ```sql
    --     not good
    insert into t values ('a','b','c');
    -- good
    insert into t(c1,c2,c3) values ('a','b','c');
 
    ```
6. 避免使用子查询，可以把子查询优化为join操作
7. 对应同一列进行or判断时，使用in代替or
9. 用UNION-ALL 替换 UNION 
8. 拆分复杂的大SQL为多个小SQL

### 设计优化

#### 单例模式
如果一个系统中，需要对一个类反复创建，则会影响性能。我们可以使用单例模式
来优化性能和系统设计，单例模式有饿汉单例和懒汉单例，这里我们直接给出线程安全并且
可以延迟加载的单例代码：
```java
public class StaticSingleton {
    private StaticSingleton(){
        System.out.println("StaticSingleton is created");
    }
    private static class SingletonHolder{
        private static final StaticSingleton INSTANCE = new StaticSingleton();
    }
    
    public static StaticSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

在上面的例子中，使用了静态内部类，即能保证线程安全，又能做到延迟加载。

需要注意的是，如果需要实现序列化，则我们需要复写`readResolve`,保证序列化前后
也是同一个实例。
```java
public class StaticSingleton implements Serializable{
    private StaticSingleton(){
        System.out.println("StaticSingleton is created");
    }
    private static class SingletonHolder{
        private static final StaticSingleton INSTANCE = new StaticSingleton();
    }
    
    public static StaticSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private Object readResolve(){
        return getInstance();
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
            // 测试序列化单例
            StaticSingleton instance = StaticSingleton.getInstance();
            FileOutputStream fos = new FileOutputStream("singleton.txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(instance);
            objectOutputStream.flush();
            objectOutputStream.close();
            FileInputStream fileInputStream = new FileInputStream("singleton.txt");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            StaticSingleton read = (StaticSingleton) objectInputStream.readObject();
            System.out.println(read==instance);
        }
}
```
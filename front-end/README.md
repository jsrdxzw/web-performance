## web 前端性能的提升方案

web前端性能的优化，是web整个应用优化的重要组成部分，
其中主要使用webpack打包工具对工程进行自动化构建优化,
使用方法：
+ ```npm install --save```
+ ```npm run build```

### 静态资源优化(Html+Css+Js)
+ 压缩js，css，html等静态资源（详见webpack的相关配置）

+ 使用WebP

    > WebP格式，是谷歌公司开发的一种旨在加快图片加载速度的图片格式。
    > 图片压缩体积大约只有JPEG的2/3，并能节省大量的服务器带宽资源和数据空间。
    > Facebook、Ebay等知名网站已经开始测试并使用WebP格式。
    > 推荐叉拍云（网址：www.upyun.com/webp ）在线压缩。
    
+ 压缩图片，这一环节也比较影响性能，比较小的图片我们可以直接转为base64编码

### 页面渲染性能优化

+ 使用CDN加速


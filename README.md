# VhallUploadKit_java
微吼上传录播并生成回放工具

一、集成方式：

复制demo/libs中的jar到工程中，add to BuildPath；

二、调用方法：

1、构建VhallUploadKit：util = VhallUploadKit.getInstance();

2、使用微吼APP_KEY,SECRET_KEY 初始化VhallUploadKit：util.initData(APP_KEY, SECRET_KEY);
![image](https://github.com/vhall/VhallUploadKit_java/blob/master/VhallJavaSDKDemo/screenshots/screenone.png)

3、上传文件并生成回放：util.uploadFile(file, videoName,subjectName,callback, new PutObjectProgressListener());

//修改 添加userId（子账号）字段，支持生成子账号下的活动，传空时默认生成主账号下活动
util.uploadFile(userId,file, videoName,subjectName,callback, new PutObjectProgressListener());

![image](https://github.com/vhall/VhallUploadKit_java/blob/master/VhallJavaSDKDemo/screenshots/screentwo.png)

4、停止上传:util.stopUpload(fileKey) 暂停此次上传操作

![image](https://github.com/vhall/VhallUploadKit_java/blob/master/VhallJavaSDKDemo/screenshots/screenthree.png)

5、取消上传：util.abortUpload(fileKey) 废弃此次上传操作
![image](https://github.com/vhall/VhallUploadKit_java/blob/master/VhallJavaSDKDemo/screenshots/screenfour.png)

三、注意事项：
1、支持的文件格式：rmvb mp4 avi wmv mkv flv mov mp3 wav

具体调用方式，参考示例demo

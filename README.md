# M3u8Download
一个简单的M3u8视频下载器[kotlin],支持单任务多线程下载。包含开始，暂停，删除,下载列表功能。

几个关键的类
========

[M3u8Config](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/M3u8Config.kt),M3u8下载器配置，可以配置线程数，下载超时时间，下载路径等

[M3u8File](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/bean/M3u8File.kt),对应M3u8索引文件，通过这个类可以将 m3u8 url 转换成 [M3u8](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/bean/M3u8.kt),以供下载

[M3u8](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/bean/M3u8.kt) 一个M3u8代表一个完整的视频，也是[M3u8Manager](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/M3u8Manager.kt) 用来管理的基本单位

[M3u8Ts](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/bean/M3u8Ts.kt) M3u8视频切片文件，下载的基本单位.多个ts合并成一个视频

[OnM3u8DeleteListener](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/listener/OnM3u8DeleteListener.kt) 任务删除回掉接口

[OnM3u8DownloadListenr](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/listener/OnM3u8DownloadListenr.kt) 下载回掉接口

[OnM3u8FileListener](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/listener/OnM3u8FileListener.kt) 查询db里所有下载任务的回掉接口

[OnM3u8InfoListener](https://github.com/fanchen001/M3u8Download/blob/master/app/src/main/java/com/fanchen/m3u8/listener/OnM3u8InfoListener.kt) 将m3u8 url 转换成M3u8实体类的回掉接口

使用姿势
========
 ```java
 M3u8Config.context = applicationContext // set context
 M3u8Config.threadCount = 5 //  下载线程数
 M3u8Config.m3u8Path = "" // 下载路径
 //注册 M3u8File to M3u8 回掉
 M3u8Manager.registerInfoListeners(object : OnM3u8InfoListener{
            
     override fun onSuccess(m3u8File: M3u8File, infos: List<M3u8>) {//转换成功
         M3u8Manager.download(infos)//将M3u8 添加到下载任务列表
     }

     override fun onError(m3u8File: M3u8File, e: Throwable) {//转换失败
     }

 })
 M3u8Manager.download(M3u8File(""))//开始 M3u8File to M3u8
 
 
 以上是最简单的使用方式。具体其他使用方式，可以参考[次元番](https://github.com/fanchen001/Bangumi)
 

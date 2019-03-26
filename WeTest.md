已经多次经过WeTest大部分手机兼容测试

### 100%通过 ###

有个别不通过是因为手机有 `` 错误的图片 ``  （因为当前设计是整个黑屏，如果图片加载不出，所以依然是全部黑屏，WeTest就误判未UI异常）

![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/1.png)



### 可以发现这个异常图片，其实并没有任何异常问题，单纯是图片本身问题导致显示不了 ### 
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/2.png)
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/3.png)

### 然后我云调试了该手机，发现它本身相册的图片都是无效图片 ### 
![](https://raw.githubusercontent.com/zhongjhATC/AlbumCameraRecorder/master/wetest/4.png)

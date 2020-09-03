# ding2webdav

单向同步钉钉通信录至webdav。

## 用法

* 运行Jar文件

`java -jar ding2webdav.jar  config.yaml`

* Docker

`docker run -it -v $(pwd)/config.yaml:/config.yaml derekamz/dingtalkhelper /config.yaml`

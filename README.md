# dingtalkhelper

目前只支持单向导出钉钉通信录至webdav。

## 用法

* 运行Jar文件

`java -jar dingtalkhelper.jar  config.yaml`

* Docker

`docker run -it -v $(pwd)/config.yaml:/config.yaml derekamz/dingtalkhelper /config.yaml`

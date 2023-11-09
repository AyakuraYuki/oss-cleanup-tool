# oss-cleanup-tool

## 使用方法

1. 克隆本项目
2. 用 IDEA 打开项目
3. 在项目根目录打开终端
4. 执行命令 `mvn compile` 编译项目（需要使用的命令 `mvn` 来自 `Maven` 且需自行安装解决）
5. 打开代码文件 `src/main/java/cc/ayakurayuki/aliyun/oss/toolkit/CleanupOSSObject.java`，编辑配置开头的五项 private 常量
6. 运行 main 方法开始扫描 bucket 删除过期文件

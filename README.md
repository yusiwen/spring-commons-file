# spring-commons-file

File storage abstraction for Spring Boot using the strategy pattern. Supports local filesystem, MinIO (S3-compatible), Alibaba Cloud OSS, and Tencent Cloud COS.

## Features

- **`FileOperator` interface** — uniform `storageFile`/`getFileBytes`/`deleteFile`/`getFileAuthUrl` API
- **`LocalFileOperator`** — local filesystem (zero external dependencies)
- **`MinioFileOperator`** — MinIO/S3-compatible storage (optional `io.minio:minio`)
- **`AliyunFileOperator`** — Alibaba Cloud OSS (optional `com.aliyun.oss:aliyun-sdk-oss`)
- **`TencentFileOperator`** — Tencent Cloud COS (optional `com.qcloud:cos_api`)
- **Bucket ACL** — `BucketAuthEnum` for public/private access control
- **Auto-configuration** — defaults to `LocalFileOperator`, override via `FileOperator` bean

## Modules

| Module | Description |
|---|---|
| `spring-commons-file-core` | Core API: `FileOperator` interface + all implementations |
| `spring-commons-file-starter` | Spring Boot auto-configuration |
| `spring-commons-file-test` | Integration tests |

## Quick Start

```xml
<dependency>
    <groupId>cn.yusiwen.spring</groupId>
    <artifactId>spring-commons-file-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Local filesystem (default, zero config)

```java
@Autowired
private FileOperator fileOperator;

public void upload(MultipartFile file) {
    String path = fileOperator.storageFile(file);
    // path is a UUID-based filename, stored under /data/upload (Linux) or D:/upload (Windows)
}
```

### MinIO

```java
MinioConfig config = new MinioConfig("http://localhost:9000", "accessKey", "secretKey", "my-bucket");
FileOperator fileOperator = new MinioFileOperator(config);
```

### Aliyun OSS

Add `aliyun-sdk-oss` to your `pom.xml`, then:

```java
FileOperator fileOperator = new AliyunFileOperator(
    "https://oss-cn-hangzhou.aliyuncs.com", "accessKeyId", "accessKeySecret", "my-bucket");
```

### Tencent COS

Add `cos_api` to your `pom.xml`, then:

```java
FileOperator fileOperator = new TencentFileOperator(
    "ap-guangzhou", "secretId", "secretKey", "my-bucket");
```

## Requirements

- Java 8+
- Spring Boot 2.7.x

## License

MIT

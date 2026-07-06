# multi-redis-spring-boot-starter

Spring Boot Starter for connecting to multiple Redis instances/clusters from a single application. Supports **Builder pattern** and **Auto-register** mode for creating multiple `RedisTemplate` / `StringRedisTemplate` instances with different cluster configurations.

## Features

- Multiple Redis cluster configurations in a single application
- **Mode 1 - Builder pattern** (inspired by `ChatClient.Builder`): manually create `RedisTemplate` / `StringRedisTemplate` beans
- **Mode 2 - Auto-register**: automatically register beans via `ImportBeanDefinitionRegistrar`
- Standalone and Redis Cluster mode support
- Lettuce connection pool support (commons-pool2)
- Cluster topology auto-refresh
- Full compatibility with Spring Boot's official Redis Starter
- Auto-configuration with `@ConfigurationProperties`

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.hongxi</groupId>
    <artifactId>multi-redis-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Configuration

```yaml
spring:
  main:
    web-application-type: none
  data:
    redis:
      auto-register: true
      clusters:
        # Standalone mode example
        order:    # 集群名 → 生成 orderRedisTemplate
          host: localhost
          port: 6379
          lettuce:
            pool:
              max-active: 16
              max-idle: 8
              min-idle: 2
        user:    # 集群名 → 生成 userRedisTemplate
          host: localhost
          port: 6380
          lettuce:
            pool:
              max-active: 8
              max-idle: 4
              min-idle: 1
        # Redis Cluster mode example
        cache:    # 集群名 → 生成 cacheRedisTemplate
          cluster:
            nodes:
              - localhost:7001
              - localhost:7002
              - localhost:7003
            max-redirects: 3
          timeout: 5000ms
          lettuce:
            pool:
              max-active: 16
              max-idle: 8
              min-idle: 2
              max-wait: 10000ms
            cluster:
              refresh:
                adaptive: true
                period: 2000ms
        session: # 集群名 → 生成 sessionRedisTemplate
          cluster:
            nodes:
              - localhost:7011
              - localhost:7012
              - localhost:7013
            max-redirects: 3
          timeout: 5000ms
          lettuce:
            pool:
              max-active: 16
              max-idle: 8
              min-idle: 2
              max-wait: 10000ms
            cluster:
              refresh:
                adaptive: true
                period: 2000ms
```

### Mode 1 - Builder Pattern

Manually define `RedisTemplate` and `StringRedisTemplate` beans using `RedisTemplateBuilder`:

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> orderRedisTemplate(RedisTemplateBuilder builder) {
        return builder.cluster("order");
    }

    @Bean
    public RedisTemplate<String, Object> userRedisTemplate(RedisTemplateBuilder builder) {
        return builder.cluster("user");
    }

    @Bean
    public StringRedisTemplate orderStringRedisTemplate(RedisTemplateBuilder builder) {
        return builder.stringTemplate("order");
    }
}
```

### Mode 2 - Auto Register

Set `spring.data.redis.auto-register=true`, beans will be automatically registered with the naming convention:
- `{clusterName}RedisConnectionFactory` — e.g. `orderRedisConnectionFactory`
- `{clusterName}RedisTemplate` — e.g. `orderRedisTemplate`
- `{clusterName}StringRedisTemplate` — e.g. `orderStringRedisTemplate`

No code needed, just inject directly:

```java
@RestController
public class MyController {

    private final RedisTemplate<String, Object> orderRedisTemplate;
    private final StringRedisTemplate userStringRedisTemplate;

    public MyController(RedisTemplate<String, Object> orderRedisTemplate,
                        StringRedisTemplate userStringRedisTemplate) {
        this.orderRedisTemplate = orderRedisTemplate;
        this.userStringRedisTemplate = userStringRedisTemplate;
    }
}
```

> **Note**: When using auto-register mode, you may need to exclude Spring Boot's default Redis auto-configuration:
> ```java
> @SpringBootApplication(exclude = {
>     RedisAutoConfiguration.class,
>     RedisReactiveAutoConfiguration.class,
>     RedisRepositoriesAutoConfiguration.class
> })
> ```

## Sample

The `multi-redis-spring-boot-sample` module demonstrates both standalone and Redis Cluster usage with 4 Redis targets:

| Name    | Mode       | Connection            |
|---------|------------|-----------------------|
| order   | Standalone | localhost:6379        |
| user    | Standalone | localhost:6380        |
| cache   | Cluster    | localhost:7001-7003   |
| session | Cluster    | localhost:7011-7013   |

### Prerequisites

#### Standalone Redis (order & user)

```bash
brew install redis

# Start default Redis on port 6379
brew services start redis

# Start second Redis instance on port 6380
redis-server --port 6380 --daemonize yes --logfile /tmp/redis-6380.log

# Stop
redis-cli -p 6380 shutdown
```

#### Redis Cluster (cache & session)

Use the provided `redis-cluster.sh` script:

```bash
# Start all clusters
./redis-cluster.sh start all

# Start specific cluster
./redis-cluster.sh start cache      # 7001-7003
./redis-cluster.sh start session    # 7011-7013

# Check status
./redis-cluster.sh status

# Stop clusters
./redis-cluster.sh stop all
```

### Run

```bash
cd multi-redis-spring-boot-sample
mvn spring-boot:run
```

The sample is a non-web app that automatically verifies all Redis connections on startup:

```
========== Multi-Redis Sample Verification ==========
[order] Redis OK: set=hello-order-1720000000000, get=hello-order-1720000000000
[user]  Redis OK: set=hello-user-1720000000001, get=hello-user-1720000000001
[cache] Redis OK: set=hello-cache-1720000000002, get=hello-cache-1720000000002
[session] Redis OK: set=hello-session-1720000000003, get=hello-session-1720000000003
========== All verifications passed! ==========
```

### Verify with redis-cli

Standalone Redis:

```bash
# order (port 6379)
redis-cli -p 6379 GET sample:test:order

# user (port 6380)
redis-cli -p 6380 GET sample:test:user
```

Redis Cluster (need `-c` flag for cluster mode):

```bash
# cache cluster (port 7001-7003)
redis-cli -c -p 7001 GET sample:test:cache

# session cluster (port 7011-7013)
redis-cli -c -p 7011 GET sample:test:session

# The sample also writes a JSON-serialized User object to the cache cluster
redis-cli -c -p 7001 GET user
```

> The `sample:test:*` keys are set-then-deleted during verification, so they may not persist.
> The `user` key (written via `cacheRedisTemplate` with Jackson JSON serializer) will remain:
> ```
> redis-cli -c -p 7001 GET user
> # "{\"name\":\"lily\",\"age\":20,\"createDate\":\"2025-07-06T...\"}"
> ```

## Project Structure

```
multi-redis-spring-boot-starter/
├── redis-cluster.sh                         # Redis Cluster management script
├── multi-redis-spring-boot-autoconfigure/   # Auto-configuration module
├── multi-redis-spring-boot-starter/         # Starter (dependencies only)
└── multi-redis-spring-boot-sample/          # Sample project
```

## License

Apache License 2.0

&copy; [hongxi.org](http://hongxi.org)

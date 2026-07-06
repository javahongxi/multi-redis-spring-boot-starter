# multi-redis-spring-boot-sample

Sample project demonstrating how to use `multi-redis-spring-boot-starter` with auto-register mode.

## Prerequisites

Install and start two Redis instances on different ports:

```bash
# Install Redis
brew install redis

# Start default Redis on port 6379
brew services start redis

# Start second Redis instance on port 6380
redis-server --port 6380 --daemonize yes --logfile /tmp/redis-6380.log
```

## Run

```bash
mvn spring-boot:run
```

The application starts on port 8080 with two Redis clusters:
- **order** → localhost:6379
- **user** → localhost:6380

## Test

### Write to order cluster (6379)

```bash
curl -s "http://localhost:8080/set?cluster=order&key=hello&value=world"
# => {"cluster":"order","value":"world","key":"hello"}
```

### Write to user cluster (6380)

```bash
curl -s "http://localhost:8080/set?cluster=user&key=name&value=alice"
# => {"cluster":"user","value":"alice","key":"name"}
```

### Read from order cluster

```bash
curl -s "http://localhost:8080/get?cluster=order&key=hello"
# => {"cluster":"order","value":"world","key":"hello"}
```

### Read from user cluster

```bash
curl -s "http://localhost:8080/get?cluster=user&key=name"
# => {"cluster":"user","value":"alice","key":"name"}
```

## Data Isolation Verification

```
=== Redis 6379 (order) ===
GET hello => "world"
GET name  => (nil)    # user cluster data is NOT here

=== Redis 6380 (user) ===
GET hello => (nil)    # order cluster data is NOT here
GET name  => "alice"
```

Data is fully isolated between the two Redis instances.

&copy; [hongxi.org](http://hongxi.org)

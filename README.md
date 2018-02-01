# Spring Boot + Hibernate Ehcache [![Build Status](https://travis-ci.org/adamzareba/company-structure-hibernate-cache.svg)](https://travis-ci.org/adamzareba/company-structure-hibernate-cache) 

Example Spring Boot + Hibernate + Ehcache project for demonstration purposes of cache mechanism. 

## Getting started
### Prerequisites:
- Java 8
- Maven
- H2/PostgreSQL

It is possible to run application in one of two profiles:
- h2
- postgres

depending on database engine chose for testing. 

To enable cache statistics `dev` profile needs to be turned on.

### Testing database schema
![database-schema](src/main/docs/db_schema.png)

### Configuration
Cache regions are configured via ehcache.xml file. Following regions are defined:
- companies by id
```xml
<cache name="company.byId"
           maxElementsInMemory="10000" eternal="false" timeToIdleSeconds="600"
           timeToLiveSeconds="3600" overflowToDisk="true"/>
```
- companies by name
```xml
<cache name="company.byName"
           maxElementsInMemory="10000" eternal="false" timeToIdleSeconds="600"
           timeToLiveSeconds="3600" overflowToDisk="true"/>
```

To enable cache mechanism `@EnableCaching` annotation is added to configuration class. Cache beans are created via annotation:
```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactory() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);

        return cacheManagerFactoryBean;
    }

    @Bean
    public EhCacheCacheManager ehCacheCacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager();
        cacheManager.setCacheManager(ehCacheManagerFactory().getObject());
        cacheManager.setTransactionAware(true);

        return cacheManager;
    }
}
```

### Implementation

Caching rules are defined in `CompanyRepository` implementation:
- cache creation
```java
@Cacheable(value = "company.byId", key = "#id", unless = "#result != null and #result.name.toUpperCase().startsWith('TEST')")
public Company find(Long id) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Company> query = builder.createQuery(Company.class);

    Root<Company> root = query.from(Company.class);
    root.fetch(Company_.cars, JoinType.LEFT);
    Fetch<Company, Department> departmentFetch = root.fetch(Company_.departments, JoinType.LEFT);
    Fetch<Department, Employee> employeeFetch = departmentFetch.fetch(Department_.employees, JoinType.LEFT);
    employeeFetch.fetch(Employee_.address, JoinType.LEFT);
    departmentFetch.fetch(Department_.offices, JoinType.LEFT);

    query.select(root).distinct(true);
    Predicate idPredicate = builder.equal(root.get(Company_.id), id);
    query.where(builder.and(idPredicate));

    return DataAccessUtils.singleResult(entityManager.createQuery(query).getResultList());
}

@Cacheable(value = "company.byName", key = "#name", unless = "#name != null and #name.toUpperCase().startsWith('TEST')")
public Company find(String name) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Company> query = builder.createQuery(Company.class);

    Root<Company> root = query.from(Company.class);
    root.fetch(Company_.cars, JoinType.LEFT);
    Fetch<Company, Department> departmentFetch = root.fetch(Company_.departments, JoinType.LEFT);
    Fetch<Department, Employee> employeeFetch = departmentFetch.fetch(Department_.employees, JoinType.LEFT);
    employeeFetch.fetch(Employee_.address, JoinType.LEFT);
    departmentFetch.fetch(Department_.offices, JoinType.LEFT);

    query.select(root).distinct(true);
    Predicate idPredicate = builder.equal(root.get(Company_.name), name);
    query.where(builder.and(idPredicate));

    return DataAccessUtils.singleResult(entityManager.createQuery(query).getResultList());
}
```
- cache invalidation
```java
@Caching(evict = {@CacheEvict(value = "company.byId", key = "#company.id"), @CacheEvict(value = "company.byName", key = "#company.name")})
public void delete(Company company) {
    entityManager.remove(company);
}
```
- cache update
```java
@Caching(evict = {@CacheEvict(value = "company.byName", allEntries = true), @CacheEvict(value = "company.byId", key = "#result.id", condition = "#result != null and #result.name.toUpperCase().startsWith('TEST')")},
            put = {@CachePut(value = "company.byId", key = "#result.id", unless = "#result != null and #result.name.toUpperCase().startsWith('TEST')")})
public Company update(Company company) {
    return entityManager.merge(company);
}
```

### Cache statistics

Optionally it's possible to expose MBean to gather cache statistics:
```java
@Bean
public MBeanServer mBeanServer() {
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    return mBeanServer;
}

@Bean
public ManagementService managementService() {
    ManagementService managementService = new ManagementService(ehCacheCacheManager.getCacheManager(), mBeanServer(), true, true, true, true);
    managementService.init();

    return managementService;
}
```
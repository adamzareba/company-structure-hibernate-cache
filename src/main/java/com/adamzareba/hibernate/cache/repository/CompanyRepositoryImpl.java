package com.adamzareba.hibernate.cache.repository;

import com.adamzareba.hibernate.cache.model.Company;
import com.adamzareba.hibernate.cache.model.Company_;
import com.adamzareba.hibernate.cache.model.Department;
import com.adamzareba.hibernate.cache.model.Department_;
import com.adamzareba.hibernate.cache.model.Employee;
import com.adamzareba.hibernate.cache.model.Employee_;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Repository
public class CompanyRepositoryImpl implements CompanyRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
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

    @Override
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

    @Override
    public List<Company> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Company> query = builder.createQuery(Company.class);
        Root<Company> root = query.from(Company.class);
        query.select(root).distinct(true);
        TypedQuery<Company> allQuery = entityManager.createQuery(query);

        return allQuery.getResultList();
    }

    @Override
    public void create(Company company) {
        entityManager.persist(company);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "company.byName", allEntries = true), @CacheEvict(value = "company.byId", key = "#result.id", condition = "#result != null and #result.name.toUpperCase().startsWith('TEST')")},
            put = {@CachePut(value = "company.byId", key = "#result.id", unless = "#result != null and #result.name.toUpperCase().startsWith('TEST')")})
    public Company update(Company company) {
        return entityManager.merge(company);
    }

    @Override
    public void delete(Long id) {
        Company company = entityManager.find(Company.class, id);
        delete(company);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "company.byId", key = "#company.id"), @CacheEvict(value = "company.byName", key = "#company.name")})
    public void delete(Company company) {
        entityManager.remove(company);
    }
}

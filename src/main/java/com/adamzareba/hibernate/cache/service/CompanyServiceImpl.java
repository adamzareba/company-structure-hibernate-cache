package com.adamzareba.hibernate.cache.service;

import com.adamzareba.hibernate.cache.model.Company;
import com.adamzareba.hibernate.cache.repository.CompanyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    @Override
    public Company get(Long id) {
        return companyRepository.find(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Company get(String name) {
        return companyRepository.find(name);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    @Transactional
    @Override
    public void create(Company company) {
        companyRepository.create(company);
    }

    @Transactional
    @Override
    public Company update(Company company) {
        return companyRepository.update(company);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        companyRepository.delete(id);
    }

    @Transactional
    @Override
    public void delete(Company company) {
        companyRepository.delete(company);
    }
}

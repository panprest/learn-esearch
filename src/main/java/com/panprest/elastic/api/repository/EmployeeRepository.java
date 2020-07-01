package com.panprest.elastic.api.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.panprest.elastic.api.model.EmployeeModel;

public interface EmployeeRepository extends ElasticsearchRepository<EmployeeModel, String> {

	List<EmployeeModel> findByFirstname(String name);
}

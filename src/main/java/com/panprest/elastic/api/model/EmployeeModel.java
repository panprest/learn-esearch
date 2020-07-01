package com.panprest.elastic.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(indexName="kantor", type="employee", shards=2)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeModel {
	
	@Id
	private String id;
	private String firstname;
	private int age;
}

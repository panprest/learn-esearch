package com.panprest.elastic.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.panprest.elastic.api.model.EmployeeModel;
import com.panprest.elastic.api.model.User;
import com.panprest.elastic.api.repository.EmployeeRepository;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.storedscripts.GetStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.PutStoredScriptRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutComponentTemplateRequest;
import org.elasticsearch.cluster.metadata.ComponentTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.StoredScriptSource;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpringDataSolr1Application {
	
	@Autowired
	private EmployeeRepository employeeRepo;
	
	@Autowired
    private RestHighLevelClient client;
	
	private ElasticsearchClient clientE;
	
	@PostMapping("/saveCustomer")
	public String saveEmployee(@RequestBody List<EmployeeModel> employees) {
		if (employees.isEmpty()) {
			throw new NullPointerException();
		}
		employeeRepo.saveAll(employees);
		return "Success : "+employees.size();
	}
	
	@GetMapping("/getAll")
	public Iterable<EmployeeModel> findAll() {
		 Iterable<EmployeeModel> test = employeeRepo.findAll();
		 
		 return test;
	}
	
	
	@GetMapping("/findByName/{firstName}")
	public List<EmployeeModel> findByFirstName(@PathVariable String firstName) {
		return employeeRepo.findByFirstname(firstName);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SpringDataSolr1Application.class, args);
	}
	//===================================== PENAMBAHAN TEST =========================================================
    @PostMapping("/create/index")
    public void index(@RequestBody Map<String, String> index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index.get("name").toLowerCase());
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 2)
        );
        Map<String, Object> message = new HashMap<>();
        message.put("type", "text");

        Map<String,Object> keyWordMap = new HashMap<>();
        Map<String,Object> keyWordValueMap = new HashMap<>();
        keyWordValueMap.put("type","keyword");
        keyWordValueMap.put("ignore_above",256);
        keyWordMap.put("keyword",keyWordValueMap);
        message.put("fields", keyWordMap);

        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", message);
        properties.put("name", message);

        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.mapping(mapping);

        GetIndexRequest getIndexRequest = new GetIndexRequest(index.get("name").toLowerCase());
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if(!exists){
            CreateIndexResponse indexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println("response id: "+indexResponse.index());
        } else {
        	System.out.println("terdaftar");
        }
    }
    
    @GetMapping("/delete/index/{nameIndex}")
    public String deleteIndex(@PathVariable final String nameIndex) throws IOException {
    	if (client.indices().exists(new GetIndexRequest(nameIndex), RequestOptions.DEFAULT)) {
    		client.indices().delete(new DeleteIndexRequest(nameIndex), RequestOptions.DEFAULT);
    		return "terhapus";
    		
    	}
    	return "index tidak ditemukan!";
    }

    @PostMapping("/")
    public String save(@RequestBody User user) throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id(user.getUserId());
        request.source(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println("response id: "+indexResponse.getId());
        return indexResponse.getResult().name();
    }

    @PostMapping("/async")
    public void indexAsync(@RequestBody User user) throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id(user.getUserId());
        request.source(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        client.indexAsync(request, RequestOptions.DEFAULT,listener);
        System.out.println("Request submitted !!!");
    }

    @GetMapping("/{id}")
    public User read(@PathVariable final String id) throws IOException {
        GetRequest getRequest = new GetRequest("users",id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        User user = new ObjectMapper().readValue(getResponse.getSourceAsString(),User.class);
        return user;
    }

    @GetMapping("/")
    public List<User> readAll() throws IOException {
        List<User> users = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest("users");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.size(5);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for(SearchHit searchHit : searchResponse.getHits().getHits()){
            User user = new ObjectMapper().readValue(searchHit.getSourceAsString(),User.class);
            users.add(user);
        }
        return users;
    }

//    @GetMapping("/name/{field}")
//    public List<User> searchByName(@PathVariable final String field) throws IOException {
//        List<User> users = new ArrayList<>();
//        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", field)
//                .fuzziness(Fuzziness.AUTO)
//                .prefixLength(2)
//                .maxExpansions(10);
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query(matchQueryBuilder);
//        sourceBuilder.from(0);
//        sourceBuilder.size(5);
//        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.source(sourceBuilder);
//
//        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
//        for(SearchHit searchHit : searchResponse.getHits().getHits()){
//            User user = new ObjectMapper().readValue(searchHit.getSourceAsString(),User.class);
//            users.add(user);
//        }
//        return users;
//    }

    @RequestMapping(value = "/",method =RequestMethod.PUT)
    public String update(@RequestBody User user) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("users",user.getUserId());
        updateRequest.doc(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest,RequestOptions.DEFAULT);
        System.out.println(updateResponse.getGetResult());

        return updateResponse.status().name();
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public String delete(@PathVariable final String id) throws IOException {
        DeleteRequest request = new DeleteRequest("users",id);
        DeleteResponse deleteResponse = client.delete(request,RequestOptions.DEFAULT);
        return deleteResponse.getResult().name();
    }
    
    @PostMapping("/create/template")
    public String createTemplate(@RequestBody Map<Object,Object> body) throws IOException {
    	Map<Object, Object> input = (Map<Object, Object>) body.get("input");
    	PutStoredScriptRequest scripts = new PutStoredScriptRequest();
    	scripts.id(body.get("name").toString());
    	Gson gson = new Gson();
    	scripts.content(new BytesArray(gson.toJson(input)),XContentType.JSON);
    	client.putScript(scripts, RequestOptions.DEFAULT);
    	return client.getScript(new GetStoredScriptRequest("testing"), RequestOptions.DEFAULT).getSource().get().toString();
    }
    
    @PostMapping("/search/template")
    public SearchResponse searchTemplate(@RequestBody Map<String,Object> body) throws IOException {
    	Map<String, Object> template_params = new HashMap<>();
    	template_params.put("name", "Panji Prasetyo");
    	SearchResponse sr = new SearchTemplateRequestBuilder(clientE)
    	        .setScript("testing")         
    	        .setScriptType(ScriptType.STORED)     
    	        .setScriptParams(template_params)     
    	        .setRequest(new SearchRequest()).get().getResponse();
    	return sr;
    }
    
    ActionListener listener = new ActionListener<IndexResponse>() {
        @Override
        public void onResponse(IndexResponse indexResponse) {
            System.out.println(" Document updated successfully !!!");
        }

        @Override
        public void onFailure(Exception e) {
            System.out.print(" Document creation failed !!!"+ e.getMessage());
        }
    };
}
    
    


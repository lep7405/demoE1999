package com.example.demoe.Service;//package com.example.test1tomany;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demoe.Entity.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    public List<Product> fetchDataFromElasticsearch() {
        String indexName = "productsss"; // Thay thế bằng tên chỉ mục thực tế của bạn

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .build();
        try {
            SearchResponse response = elasticsearchClient.search(searchRequest, Product.class);
            List<Hit> hits = response.hits().hits();
            List<Product> products = new ArrayList<>();
            for(Hit object : hits){

                System.out.print(((Product) object.source()));
                products.add((Product) object.source());

            }
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý các ngoại lệ nếu cần thiết
            return null; // Hoặc bạn có thể throw Exception để xử lý ngoại lệ ở lớp gọi
        }
    }
    public List<IndexResponse> indexProducts(List<Product> productList) {
        List<IndexResponse> ids = new ArrayList<>();
        for (Product productDocument : productList) {
            try {
                IndexRequest<Product> request = new IndexRequest.Builder<Product>()
                        .index("productsss") // Thay thế bằng tên chỉ mục thực tế của bạn
                        .id(String.valueOf(productDocument.getId())) // ID của document
                        .document(productDocument)
                        .build();

                IndexResponse response = elasticsearchClient.index(request);
                ids.add(response);
            } catch (Exception e) {
                e.printStackTrace();
                ids.add(null); // Thêm giá trị null để biết tài liệu nào bị lỗi
            }
        }
        return ids;
    }
    public List<Product> prefixProduct(String productNamePrefix) {
        List<Product> results = new ArrayList<>();

        try {
            // Tạo SearchRequest
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("productss") // Chỉ mục bạn cần tìm kiếm
                    .query(q -> q
                            .prefix(p -> p
                                    .field("productName") // Trường cần tìm kiếm
                                    .value(productNamePrefix) // Giá trị prefix
                            )
                    )
                    .build();

            // Thực hiện tìm kiếm
            SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

            // Xử lý kết quả tìm kiếm
            for (var hit : searchResponse.hits().hits()) {
                results.add(hit.source());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<Map<String, String>> prefixProduct2(String productNamePrefix) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Tạo SearchRequest
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("productss") // Chỉ mục bạn cần tìm kiếm
                    .query(q -> q
                            .prefix(p -> p
                                    .field("productName") // Trường cần tìm kiếm
                                    .value(productNamePrefix) // Giá trị prefix
                            )
                    )
                    .build();

            // Thực hiện tìm kiếm
            SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

            // Xử lý kết quả tìm kiếm
            for (var hit : searchResponse.hits().hits()) {
                Map<String, String> product = new HashMap<>();
                product.put("productName", hit.source().getProductName());
                product.put("id", String.valueOf(hit.source().getId()));
                results.add(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<Map<String, String>> top10ProductCountSearch(String productNamePrefix) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Tạo SearchRequest
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("autocomplete_index6") // Chỉ mục bạn cần tìm kiếm
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .matchPhrase(ma -> ma
                                                    .field("productName")
                                                    .query(productNamePrefix)
                                                    
                                            )
                                    )
                                    .must(m -> m
                                            .nested(n -> n
                                                    .path("countProduct.countPerMonths")
                                                    .query(nq -> nq
                                                            .bool(nb -> nb
                                                                    .must(mb -> mb
                                                                            .term(t -> t
                                                                                    .field("countProduct.countPerMonths.dateCount")
                                                                                    .value(2024)
                                                                            )
                                                                    )
                                                                    .must(mb -> mb
                                                                            .term(t -> t
                                                                                    .field("countProduct.countPerMonths.dateCount")
                                                                                    .value(8)
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
                    .sort(s -> s
                            .field(f -> f
                                    .field("countProduct.countPerMonths.countPro")
                                    .order(SortOrder.Asc)
                                    .nested(n -> n
                                            .path("countProduct.countPerMonths")
                                    )
                            )
                    )
                    .size(10) // Kích thước của kết quả trả về (ví dụ: top 10)
                    .build();

            // Thực hiện tìm kiếm
            SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

            // Xử lý kết quả tìm kiếm
            for (var hit : searchResponse.hits().hits()) {
                Map<String, String> product = new HashMap<>();
                product.put("productName", hit.source().getProductName());
                product.put("id", String.valueOf(hit.source().getId()));
                results.add(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<Map<String, String>> recommentSearch(String productNamePrefix) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Tạo SearchRequest
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("products") // Chỉ mục bạn cần tìm kiếm
                    .query(q -> q
                            .match(m -> m
                                    .field("productName") // Trường cần tìm kiếm
                                    .query(productNamePrefix) // Giá trị prefix
                                    .operator(Operator.And) // Sử dụng toán tử AND cho tìm kiếm
                            )
                    )
                    .build();

            // Thực hiện tìm kiếm
            SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

            // Xử lý kết quả tìm kiếm
            for (var hit : searchResponse.hits().hits()) {
                Map<String, String> product = new HashMap<>();
                product.put("productName", hit.source().getProductName());
                product.put("id", String.valueOf(hit.source().getId()));
                results.add(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
}



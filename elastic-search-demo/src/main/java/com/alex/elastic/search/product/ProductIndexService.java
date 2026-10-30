package com.alex.elastic.search.product;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProductIndexService {


    private final ElasticsearchClient client;

    public ProductIndexService(ElasticsearchClient client) {
        this.client = client;
    }

    public String index(ProductDocument product) throws IOException {
        if (product.id() == null || product.id().isBlank()) {
            throw new IllegalArgumentException(
                    "Product ID must not be blank");
        }

        IndexResponse response = client.index(i -> i
                .index(ProductConstant.INDEX.getValue())
                .id(product.id())
                .document(product)
        );

        return response.result().jsonValue();
    }

    public void delete(String id) throws IOException {
        client.delete(d -> d
                .index(ProductConstant.INDEX.getValue())
                .id(id)
        );
    }
}
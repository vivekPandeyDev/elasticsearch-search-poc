package com.alex.elastic.search.product;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ProductSearchService {

    public record SearchCriteria(
            String text,
            String brand,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean inStock,
            String sort,
            String direction,
            int page,
            int size,
            boolean fuzzy) {
    }


    private final ElasticsearchClient client;

    public ProductSearchService(ElasticsearchClient client) {
        this.client = client;
    }

    public ProductSearchResult search(SearchCriteria criteria) throws IOException {

        String text = criteria.text();
        String brand = criteria.brand();
        String category = criteria.category();
        Double minPrice = criteria.minPrice();
        Double maxPrice = criteria.maxPrice();
        Boolean inStock = criteria.inStock();
        String sort = criteria.sort();
        String direction = criteria.direction();
        int page = criteria.page();
        int size = criteria.size();
        boolean fuzzy = criteria.fuzzy();

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100");
        }

        if (minPrice != null && maxPrice != null
                && minPrice > maxPrice) {
            throw new IllegalArgumentException(
                    "minPrice cannot be greater than maxPrice");
        }

        if (sort != null
                && !sort.isBlank()
                && !sort.equals("price")
                && !sort.equals("rating")) {
            throw new IllegalArgumentException(
                    "Sort must be price or rating");
        }

        if (direction != null
                && !direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException(
                    "Direction must be asc or desc");
        }

        Query query = buildQuery(
                text, brand, category, minPrice,
                maxPrice, inStock, fuzzy);

        SearchRequest.Builder request = new SearchRequest.Builder()
                .index(ProductConstant.INDEX.getValue())
                .query(query)
                .from(Math.multiplyExact(page, size))
                .size(size);

        // No explicit sort means Elasticsearch relevance order.
        if (sort != null && !sort.isBlank()) {
            SortOrder order = "desc".equalsIgnoreCase(direction)
                    ? SortOrder.Desc
                    : SortOrder.Asc;

            request.sort(s -> s.field(f -> f
                    .field(sort)
                    .order(order)));
        }

        SearchResponse<ProductDocument> response = client.search(request.build(), ProductDocument.class);

        List<ProductDocument> products = response.hits()
                .hits()
                .stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        long total = response.hits().total() == null
                ? products.size()
                : response.hits().total().value();

        return new ProductSearchResult(products, total, page, size);
    }

    private Query buildQuery(
            String text,
            String brand,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean inStock,
            boolean fuzzy) {

        BoolQuery.Builder bool = new BoolQuery.Builder();
        addTextQuery(bool, text, fuzzy);
        addExactMatchFilters(bool, brand, category, inStock);
        addPriceFilter(bool, minPrice, maxPrice);

        return Query.of(q -> q.bool(bool.build()));
    }

    private void addTextQuery(BoolQuery.Builder bool, String text, boolean fuzzy) {
        if (text == null || text.isBlank()) {
            bool.must(Query.of(q -> q.matchAll(m -> m)));
        } else {
            MultiMatchQuery.Builder multiMatch = new MultiMatchQuery.Builder()
                    .query(text)
                    .fields("name^3", "description");

            if (fuzzy) {
                multiMatch.fuzziness("AUTO");
            }

            bool.must(Query.of(q -> q.multiMatch(multiMatch.build())));
        }
    }

    private void addExactMatchFilters(
            BoolQuery.Builder bool, String brand, String category, Boolean inStock) {
        // Exact-match filters
        if (brand != null && !brand.isBlank()) {
            bool.filter(Query.of(q -> q.term(t -> t
                    .field("brand")
                    .value(FieldValue.of(brand)))));
        }

        if (category != null && !category.isBlank()) {
            bool.filter(Query.of(q -> q.term(t -> t
                    .field("category")
                    .value(FieldValue.of(category)))));
        }

        if (inStock != null) {
            bool.filter(Query.of(q -> q.term(t -> t
                    .field("inStock")
                    .value(FieldValue.of(inStock)))));
        }
    }

    private void addPriceFilter(BoolQuery.Builder bool, Double minPrice, Double maxPrice) {
        // Numeric range filter
        if (minPrice != null || maxPrice != null) {
            bool.filter(Query.of(q -> q.range(r -> r.number(n -> {
                n.field("price");

                if (minPrice != null) {
                    n.gte(minPrice);
                }

                if (maxPrice != null) {
                    n.lte(maxPrice);
                }

                return n;
            }))));
        }
    }
}

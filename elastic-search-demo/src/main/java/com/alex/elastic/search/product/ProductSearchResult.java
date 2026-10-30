package com.alex.elastic.search.product;

import java.util.List;

public record ProductSearchResult(
        List<ProductDocument> items,
        long total,
        int page,
        int size) {
}

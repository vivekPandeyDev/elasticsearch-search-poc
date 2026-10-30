package com.alex.elastic.search.product;

public record ProductDocument(
        String id,
        String name,
        String description,
        String brand,
        String category,
        double price,
        double rating,
        boolean inStock) {

}

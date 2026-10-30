package com.alex.elastic.search.product;

public enum ProductConstant {
    INDEX("products")
    ;

    private final String value;

    ProductConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
package com.alex.elastic.search.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductSearchController {

    private final ProductIndexService indexService;
    private final ProductSearchService searchService;

    public ProductSearchController(
            ProductIndexService indexService,
            ProductSearchService searchService) {
        this.indexService = indexService;
        this.searchService = searchService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> indexProduct(
            @RequestBody ProductDocument product) throws IOException {

        String result = indexService.index(product);

        return Map.of(
                "id", product.id(),
                "result", result);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id)
            throws IOException {
        indexService.delete(id);
    }

    @GetMapping("/search")
    public ProductSearchResult search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean fuzzy) throws IOException {

        var criteria = new ProductSearchService.SearchCriteria(
                q,
                brand,
                category,
                minPrice,
                maxPrice,
                inStock,
                sort,
                direction,
                page,
                size,
                fuzzy);
        return searchService.search(criteria);
    }
}
# Elasticsearch Search PoC

A product search application built with **Java, Spring Boot 4, Maven, and Elasticsearch**. This project demonstrates how to build a website search system similar to the search functionality found in e-commerce platforms such as Amazon and Flipkart.

The focus is on conventional search techniques, including full-text search, relevance scoring, filtering, sorting, pagination, and fuzzy matching—without using AI, LLMs, embeddings, or vector databases.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Search Examples](#search-examples)
- [Elasticsearch Concepts](#elasticsearch-concepts)
- [Configuration](#configuration)
- [Limitations](#limitations)
- [Future Improvements](#future-improvements)
- [References](#references)

## Overview

Traditional database queries are well suited for retrieving structured records, but website search often requires additional capabilities:

- Searching across multiple text fields.
- Ranking results by relevance.
- Filtering results by product attributes.
- Handling spelling mistakes.
- Sorting and paginating large result sets.
- Supporting fast, interactive search experiences.

Elasticsearch addresses these requirements using an inverted index, text analysis, and relevance-scoring algorithms.

This project implements a REST API that indexes product documents in Elasticsearch and exposes endpoints for searching and managing them.

### Example Use Case

A user searches for:

`Nike running shoes`

The search engine retrieves matching products and ranks them based on relevance.

The user can further narrow the results by applying filters:

- Brand: Nike
- Minimum price: 2,000
- Maximum price: 4,000
- Availability: In stock

The resulting API combines full-text search with structured filters.

## Features

### Product Indexing

- Index product documents in Elasticsearch.
- Use product IDs as Elasticsearch document IDs.
- Update existing documents through repeated indexing.
- Delete products from the search index.
- Automatically create the initial index with explicit mappings.

### Full-Text Search

- Search product names and descriptions.
- Use Elasticsearch's relevance scoring.
- Boost product-name matches over description matches.
- Return results ranked by relevance when no explicit sort is provided.

### Filtering

- Filter by brand.
- Filter by category.
- Filter by stock availability.
- Filter by minimum and maximum price.

### Sorting and Pagination

- Sort by price.
- Sort by rating.
- Support ascending and descending order.
- Retrieve results using page number and page size.
- Return the total number of matching documents.

### Fuzzy Search

- Allow approximate text matching.
- Handle some spelling mistakes in search queries.
- Compare fuzzy results with normal full-text search.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4 | Application framework |
| Maven | Build and dependency management |
| Spring Web MVC | REST API development |
| Spring Boot Elasticsearch starter | Elasticsearch client integration |
| Elasticsearch | Search and indexing engine |
| Docker Compose | Local Elasticsearch environment |
| Spring Boot Actuator | Application health endpoint |
| cURL | API testing |

## Architecture

```text
                 Client / Browser
                        |
                        v
                 REST API :8080
                        |
                        v
              ProductSearchController
                        |
               +--------+--------+
               |                 |
               v                 v
       ProductIndexService  ProductSearchService
               |                 |
               +--------+--------+
                        |
                        v
              ElasticsearchClient
                        |
                        v
                Elasticsearch :9200
                        |
                        v
                  products index
```

### Request Flow

**Indexing a product**

1. The client sends a product document to the REST API.
2. The controller delegates the request to `ProductIndexService`.
3. The service indexes the document using `ElasticsearchClient`.
4. Elasticsearch stores the document in the `products` index.

**Searching products**

1. The client sends a search query and optional filters.
2. The controller passes the parameters to `ProductSearchService`.
3. The service builds an Elasticsearch Query DSL request.
4. Elasticsearch evaluates the query and ranks matching documents.
5. The API returns matching products and pagination metadata.

## Project Structure

```text
elasticsearch-search-poc/
├── docker-compose.yml
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/search/
        │       ├── SearchApplication.java
        │       ├── config/
        │       │   └── ElasticsearchIndexConfig.java
        │       └── product/
        │           ├── ProductDocument.java
        │           ├── ProductIndexService.java
        │           ├── ProductSearchService.java
        │           ├── ProductSearchController.java
        │           └── ProductSearchResult.java
        └── resources/
            └── application.yml
```

### Package Responsibilities

| Component | Responsibility |
|---|---|
| `SearchApplication` | Application entry point |
| `ElasticsearchIndexConfig` | Creates the index and mappings if the index does not exist |
| `ProductDocument` | Represents the indexed product document |
| `ProductIndexService` | Indexes and deletes products |
| `ProductSearchService` | Builds and executes search queries |
| `ProductSearchController` | Exposes REST endpoints |
| `ProductSearchResult` | Represents search results and pagination metadata |

## Prerequisites

Install the following:

- Java 21 or later.
- Maven 3.9 or later.
- Docker Engine.
- Docker Compose.

Verify your installations:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd elasticsearch-search-poc
```

Replace `<your-repository-url>` with your Git repository URL.

### 2. Start Elasticsearch

The project uses Docker Compose to run Elasticsearch locally.

```bash
docker compose up -d
```

Check the container:

```bash
docker compose ps
```

Inspect its logs if necessary:

```bash
docker compose logs -f elasticsearch
```

Verify Elasticsearch:

```bash
curl http://localhost:9200
```

A successful response contains information about the running Elasticsearch node and cluster.

> **Security warning:** The local Docker configuration disables Elasticsearch security. Use this configuration only in a trusted development environment. Production deployments should enable authentication and TLS.

### 3. Start the Spring Boot Application

Build the application:

```bash
mvn clean package
```

Run it:

```bash
mvn spring-boot:run
```

Alternatively, run the packaged JAR:

```bash
java -jar target/elasticsearch-search-poc-0.0.1-SNAPSHOT.jar
```

The application starts on port `8080`.

### 4. Verify Application Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

The health response depends on the configured Actuator health indicators and application state.

### 5. Index Sample Products

#### Nike Air Max Running Shoes

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "P101",
    "name": "Nike Air Max Running Shoes",
    "description": "Lightweight cushioned shoes for long-distance running",
    "brand": "Nike",
    "category": "Running Shoes",
    "price": 3499,
    "rating": 4.5,
    "inStock": true
  }'
```

#### Adidas Ultraboost Running Shoes

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "P102",
    "name": "Adidas Ultraboost Running Shoes",
    "description": "Comfortable running shoes with responsive cushioning",
    "brand": "Adidas",
    "category": "Running Shoes",
    "price": 4999,
    "rating": 4.7,
    "inStock": true
  }'
```

#### Nike Revolution Road Shoes

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "P103",
    "name": "Nike Revolution Road Shoes",
    "description": "Affordable lightweight shoes for daily road running",
    "brand": "Nike",
    "category": "Running Shoes",
    "price": 2299,
    "rating": 4.2,
    "inStock": false
  }'
```

#### Logitech Wireless Mouse

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "P104",
    "name": "Logitech Wireless Mouse",
    "description": "Wireless computer mouse with ergonomic design",
    "brand": "Logitech",
    "category": "Electronics",
    "price": 799,
    "rating": 4.4,
    "inStock": true
  }'
```

#### Puma Velocity Running Shoes

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "P105",
    "name": "Puma Velocity Running Shoes",
    "description": "Lightweight athletic footwear for everyday training",
    "brand": "Puma",
    "category": "Running Shoes",
    "price": 2999,
    "rating": 4.1,
    "inStock": true
  }'
```

Indexing the same ID again replaces the existing indexed document.

Elasticsearch is near-real-time, so a newly indexed document might not appear in search immediately. Retry the search after a short delay if necessary.

## API Reference

Base URL:

```text
http://localhost:8080
```

### 1. Index a Product

**Endpoint**

```http
POST /api/products
```

**Request body**

```json
{
  "id": "P106",
  "name": "Asics Gel Running Shoes",
  "description": "Comfortable shoes for daily running",
  "brand": "Asics",
  "category": "Running Shoes",
  "price": 3999,
  "rating": 4.6,
  "inStock": true
}
```

**Purpose**

Creates a new product document or replaces the existing document with the same ID.

### 2. Search Products

**Endpoint**

```http
GET /api/search?q=running
```

**Purpose**

Searches product names and descriptions using full-text matching.

The product name has a higher relevance boost than the description.

### 3. Filter Products by Brand

```http
GET /api/search?q=shoes&brand=Nike
```

Returns matching products with an exact brand match.

### 4. Filter by Price Range

```http
GET /api/search?q=shoes&minPrice=2000&maxPrice=4000
```

Filters matching products to the specified inclusive price range.

### 5. Filter by Stock Availability

```http
GET /api/search?q=shoes&inStock=true
```

Returns matching products that are in stock.

### 6. Filter by Category

```http
GET /api/search?category=Running%20Shoes
```

Returns products belonging to the specified category. With no text query, the current implementation uses a match-all query and applies the supplied filters.

### 7. Sort by Price

Ascending:

```http
GET /api/search?q=shoes&sort=price&direction=asc
```

Descending:

```http
GET /api/search?q=shoes&sort=price&direction=desc
```

Supported sort fields:

- `price`
- `rating`

### 8. Pagination

First page:

```http
GET /api/search?q=shoes&page=0&size=2
```

Second page:

```http
GET /api/search?q=shoes&page=1&size=2
```

The page number is zero-based. The current API restricts page size to 1–100.

### 9. Fuzzy Search

```http
GET /api/search?q=nik%20running%20shose&fuzzy=true
```

Enables Elasticsearch fuzzy matching to recover some results for misspelled words.

Fuzzy matching can increase query cost and return less precise results, so its behavior should be evaluated against representative search queries.

### 10. Delete a Product

```http
DELETE /api/products/P101
```

Removes the product document from the Elasticsearch index.

## Search Examples

The following examples demonstrate how search parameters can be combined.

### Search Nike Products Within a Price Range

```bash
curl -G http://localhost:8080/api/search \
  --data-urlencode "q=running shoes" \
  --data-urlencode "brand=Nike" \
  --data-urlencode "minPrice=2000" \
  --data-urlencode "maxPrice=4000"
```

### Find Available Products and Sort by Price

```bash
curl -G http://localhost:8080/api/search \
  --data-urlencode "q=running shoes" \
  --data-urlencode "inStock=true" \
  --data-urlencode "sort=price" \
  --data-urlencode "direction=asc"
```

### Search With Pagination

```bash
curl -G http://localhost:8080/api/search \
  --data-urlencode "q=shoes" \
  --data-urlencode "page=0" \
  --data-urlencode "size=5"
```

### Example Response

The search API returns a structure similar to:

```json
{
  "items": [
    {
      "id": "P101",
      "name": "Nike Air Max Running Shoes",
      "description": "Lightweight cushioned shoes for long-distance running",
      "brand": "Nike",
      "category": "Running Shoes",
      "price": 3499.0,
      "rating": 4.5,
      "inStock": true
    }
  ],
  "total": 1,
  "page": 0,
  "size": 10
}
```

The actual products and total count depend on the indexed documents and the search parameters.

## Elasticsearch Concepts

### 1. Index

An index is a logical collection of documents that Elasticsearch searches.

This project uses:

```text
products
```

### 2. Document

A document is an individual JSON record stored in an index.

For this project, each document represents a product.

### 3. Mapping

Mappings define how Elasticsearch stores and searches each field.

Examples:

- `text`: Analyzed full-text fields.
- `keyword`: Exact-match fields.
- `double`: Numeric prices.
- `float`: Numeric ratings.
- `boolean`: Stock availability.

Explicit mappings prevent important fields from receiving inappropriate automatically inferred types.

### 4. Inverted Index

Elasticsearch uses an inverted index to map analyzed terms to documents containing those terms.

For example, a product name such as:

`Nike Air Max Running Shoes`

is analyzed into searchable terms. Elasticsearch can use its inverted index to locate matching documents without scanning every document in the index for each query.

### 5. Relevance Scoring

Elasticsearch calculates relevance scores for matching documents. Its default similarity algorithm is BM25.

The current implementation boosts the product name:

```java
.fields("name^3", "description")
```

This gives name matches greater influence than description matches, although the final score depends on the query, field statistics, and other scoring factors.

### 6. Filters

Filters restrict which documents qualify for a search.

Examples include:

- `brand = Nike`
- `category = Running Shoes`
- `price >= 2000`
- `inStock = true`

Filters are especially useful for e-commerce faceted search because they can narrow the candidate products without relying on full-text relevance.

### 7. Fuzzy Matching

Fuzzy matching uses edit-distance-based matching to tolerate certain spelling mistakes.

It is useful for queries such as:

`shose` instead of `shoes`

However, fuzzy search is not equivalent to autocomplete, synonym expansion, or semantic search.

## Configuration

The default application configuration is:

```yaml
spring:
  application:
    name: elasticsearch-search-poc

  elasticsearch:
    uris: ${ELASTICSEARCH_URL:http://localhost:9200}

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Elasticsearch URL

By default, the application connects to:

```text
http://localhost:9200
```

Override the endpoint with an environment variable:

```bash
export ELASTICSEARCH_URL=http://localhost:9200
mvn spring-boot:run
```

For a secured Elasticsearch deployment, configure authentication and TLS using the supported Spring Boot Elasticsearch client properties and credentials appropriate to your environment.

## Limitations

This is a learning PoC, not a production-ready search platform.

Current limitations include:

- No authentication or authorization on product-management endpoints.
- No request DTO validation or centralized API exception handling.
- No autocomplete endpoint.
- No search-result aggregations or facet counts.
- No explicit custom analyzers, synonyms, or language-specific analysis.
- No PostgreSQL source of truth or automatic synchronization.
- No dedicated index migration or reindexing workflow.
- No automated integration tests against Elasticsearch.
- No performance benchmarks or relevance evaluation dataset.
- Offset pagination, which is not suitable for arbitrarily deep result pages.

The local Docker setup disables Elasticsearch security and must not be exposed to untrusted networks.

## Future Improvements

### Phase 1: Search Experience

- Implement autocomplete and search suggestions.
- Configure analyzers and tokenization.
- Add synonym support.
- Improve fuzzy matching and typo tolerance.
- Add aggregations for brand and category facets.
- Return relevance scores and useful search metadata.

### Phase 2: Data Management

- Introduce PostgreSQL as the source of truth.
- Synchronize product changes into Elasticsearch.
- Use a transactional outbox or CDC for reliable synchronization.
- Handle retries, duplicate events, deletions, and stale documents.
- Support index versioning, aliases, and reindexing.

### Phase 3: Testing and Performance

- Add unit and integration tests.
- Use Testcontainers for Elasticsearch integration tests.
- Build a representative product dataset.
- Evaluate search relevance using expected queries and results.
- Measure latency under realistic workloads.
- Introduce cursor-based pagination with `search_after` for deep navigation.

### Phase 4: Advanced Search

- Compare BM25 with synonym-enhanced search.
- Evaluate semantic search using embeddings.
- Experiment with hybrid search combining lexical and semantic retrieval.
- Add AI only where it demonstrates measurable improvements over conventional search.

## References

- [Elasticsearch Documentation](https://www.elastic.co/docs)
- [Elasticsearch Query DSL](https://www.elastic.co/docs/reference/query-languages/query-dsl)
- [Elasticsearch Java API Client](https://www.elastic.co/docs/reference/elasticsearch/clients/java)
- [Spring Boot Elasticsearch Documentation](https://docs.spring.io/spring-boot/4.0/reference/data/nosql.html)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## License

This project is intended for learning and experimentation. Add a license appropriate to your intended use before distributing it publicly.
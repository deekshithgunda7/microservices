package com.springmicroservicepoc.productservice.repository;

import com.springmicroservicepoc.productservice.modal.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}

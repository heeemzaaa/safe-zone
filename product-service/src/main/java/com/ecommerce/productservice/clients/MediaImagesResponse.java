package com.ecommerce.productservice.clients;

import java.util.List;

// test here to see
public record MediaImagesResponse(
  boolean success, 
  String message, 
  List<String> data) {
}
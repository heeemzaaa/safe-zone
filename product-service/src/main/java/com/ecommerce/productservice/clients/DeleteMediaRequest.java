package com.ecommerce.productservice.clients;

import java.util.List;


// test product service
// second test 2
public record DeleteMediaRequest(String targetType, String targetId, List<String> imagePaths) {
}

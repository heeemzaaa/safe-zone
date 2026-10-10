package com.ecommerce.productservice.clients;

import java.util.List;


// test product service
// second test 4
public record DeleteMediaRequest(String targetType, String targetId, List<String> imagePaths) {
}

package com.ecommerce.productservice.clients;

import java.util.List;

// second test
public record DeleteMediaRequest(String targetType, String targetId, List<String> imagePaths) {
}

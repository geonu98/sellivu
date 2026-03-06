package com.sellivu.backend.platform;

public interface ProductUrlParser {
    boolean supports(String url);
    ProductUrlInfo parse(String url);
}
package com.ms.hoopi.product.service.serviceImpl;

import com.ms.hoopi.model.entity.Product;
import com.ms.hoopi.model.entity.ProductImg;
import com.ms.hoopi.product.model.ProductDetailResponseDto;
import com.ms.hoopi.product.model.ProductImgResponseDto;
import com.ms.hoopi.product.model.ProductResponseDto;
import com.ms.hoopi.product.service.ProductService;
import com.ms.hoopi.repository.ArticleRepository;
import com.ms.hoopi.repository.ProductImgRepository;
import com.ms.hoopi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponseDto> getProductsNew() {
        List<ProductDetailResponseDto> productNew = new ArrayList<>();

        List<ProductResponseDto> product = productRepository.findAllNew();
        for(ProductResponseDto productDto : product){
            ProductImgResponseDto productImgDto = productImgRepository.findByProductCode(productDto.getProductCode());
            String boardContent = articleRepository.findByProductCode(productDto.getProductCode());
            ProductDetailResponseDto productDetailDto = ProductDetailResponseDto.builder()
                                                                                .product(productDto)
                                                                                .productImg(productImgDto)
                                                                                .boardContent(boardContent)
                                                                                .build();
            productNew.add(productDetailDto);
        }
        return productNew;
    }
}
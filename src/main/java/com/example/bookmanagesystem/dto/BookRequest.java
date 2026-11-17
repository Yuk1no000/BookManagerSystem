package com.example.bookmanagesystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BookRequest(
        @NotBlank(message = "书名不能为空")
        @Size(max=100,message = "书名最多100字符")
        String name,

        @NotBlank(message = "作者不能为空")
        @Size(max=100,message = "作者名最多100字符")
        String author,

        @NotNull(message = "价格不能为空")
        @PositiveOrZero(message = "价格不能为负数")
        @Digits(integer = 8,fraction = 2,message="价格最多8位整数，两位小数")
        BigDecimal price,

        @NotBlank(message = "描述不能为空")
        @Size(max=100,message = "描述最多一百字符")
        String description

) {
}

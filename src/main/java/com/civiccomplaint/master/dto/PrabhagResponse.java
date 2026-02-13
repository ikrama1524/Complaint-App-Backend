package com.civiccomplaint.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrabhagResponse {
    private Integer id;
    private String name;
    private String code;
    private String description;
}

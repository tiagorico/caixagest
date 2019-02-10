/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.johnzon.mapper.JohnzonProperty;

/**
 * TODO add a description here
 *
 * @author rico
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RatingDto {
    @JohnzonProperty("y")
    private Double value;
    @JohnzonProperty("x")
    private String date;
}

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

/**
 * TODO add a description here
 *
 * @author rico
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FundDto {
    private int id;
    private String name;
}

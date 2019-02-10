/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.model.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

/**
 * TODO add a description here
 *
 * @author rico
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class RatingID implements Serializable {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "fund_id", foreignKey = @ForeignKey(name = "FK_RATING_FUND"))
    private Fund fund;

    @Column(name = "date", nullable = false)
    private LocalDate date;
}

/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.model.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

/**
 * This class represents the Rate entity.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode
@Entity
@Table(name = "rate")
public class Rate implements Serializable {
    @EmbeddedId
    private RateID id;

    @Column(name = "value", nullable = false)
    private Double value;
}

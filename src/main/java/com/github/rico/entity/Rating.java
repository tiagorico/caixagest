/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PRIVATE;

/**
 * TODO add a description here
 *
 * @author rico
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode
@Entity
@Table(name = "RATING")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "rating_sequence", sequenceName = "RATING_SEQUENCE_GENERATOR")
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "DATE", unique = true, nullable = false)
    private LocalDate date;

    @Column(name = "VALUE", nullable = false)
    private Double value;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "FUND_ID", foreignKey = @ForeignKey(name = "FK_RATING_FUND"))
    private Fund fund;
}

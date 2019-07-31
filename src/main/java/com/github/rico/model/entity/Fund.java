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
import java.util.Set;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * This class represents the Fund entity.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@Setter
@Getter
@Builder
@ToString(exclude = "rates")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(exclude = {"id", "name", "status", "rates"})
@Entity
@Table(name = "fund")
public class Fund implements Serializable {

    /**
     * Status enum for fund.
     */
    public enum Status {
        DISABLE, ENABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "fund_sequence", sequenceName = "fund_sequence_generator")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "uuid", length = 30, unique = true, nullable = false)
    private UUID uuid;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", length = 7, nullable = false)
    private Status status;

    @OneToMany(mappedBy = "id.fund", cascade = CascadeType.ALL)
    @OrderBy(value = "id.date")
    private Set<Rate> rates;

    @Transient
    private Double min;

    @Transient
    private Double max;

    @Transient
    private Double average;

    @Transient
    private Double standardDeviation;

    @Transient
    private Double yesterday;

    @Transient
    private Double today;

    @Transient
    private Double dayBeforeYesterday;

    public Fund(final Integer id,
                final String name,
                final Double max,
                final Double min,
                final Double average,
                final Double standardDeviation,
                final Double today,
                final Double yesterday,
                final Double dayBeforeYesterday) {
        this.id = id;
        this.name = name;
        this.min = min;
        this.max = max;
        this.average = average;
        this.standardDeviation = standardDeviation;
        this.yesterday = yesterday;
        this.today = today;
        this.dayBeforeYesterday = dayBeforeYesterday;
    }
}

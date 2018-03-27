/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.EAGER;

/**
 * TODO add a description here
 *
 * @author rico
 */
@Getter @Setter @Builder @ToString @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
@Entity @Table(name = "SHARE_RATING")
public class ShareRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "date", unique = true, nullable = false)
    private LocalDateTime date;

    @Column(name = "value", nullable = false)
    private Double value;

    @ManyToOne(fetch = EAGER, optional = false)
    @JoinColumn(name = "fundId", nullable = false,
            foreignKey = @ForeignKey(name = "FK_SHARE_RATING_FUND"))
    private Fund fund;
}

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
import java.util.Set;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * Entity for fund
 *
 * @author rico
 */
@Getter
@Setter
@Builder
@ToString(exclude = "ratings")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(exclude = {"id", "name", "status", "ratings"})
@Entity
@Table(name = "FUND")
public class Fund {

    public enum Status {
        DISABLE, ENABLE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "fund_sequence", sequenceName = "FUND_SEQUENCE_GENERATOR")
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "UUID", length = 30, unique = true, nullable = false)
    private UUID uuid;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", length = 7, nullable = false)
    private Status status;

    @OneToMany(mappedBy = "fund", cascade = CascadeType.ALL)
    @OrderBy(value = "date")
    private Set<Rating> ratings;

}

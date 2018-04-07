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
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * Entity for fund
 *
 * @author rico
 */
@Setter
@Getter
@Builder
@ToString(exclude = "ratings")
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(exclude = {"id", "name", "status", "ratings"})
@Entity
@Table(name = "fund")
public class Fund implements Serializable {

    public enum Status {
        DISABLE, ENABLE;
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
    private Set<Rating> ratings;

}

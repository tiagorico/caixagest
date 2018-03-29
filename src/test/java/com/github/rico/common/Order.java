/*
 * Copyright (c) Stratio Lda., All Rights Reserved.
 * (www.stratio.pt)
 *
 * This software is the proprietary information of Stratio Lda.
 * Use is subject to license terms.
 */
package com.github.rico.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface annotation
 * <p>
 * Created by Rico on 02/03/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Order {
    int order();
}

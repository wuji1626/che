/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.schedule;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Mark method for execution  a periodic action that becomes enabled first after the given initial delay, and
 * subsequently with the given period; that is executions will commence after initialDelay then initialDelay+period,
 * then initialDelay + 2 * period, and so on. If any execution of the task encounters an exception, subsequent
 * executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
 * If any execution of this task takes longer than its period, then subsequent executions may start late, but will not
 * concurrently execute.
 * <p/>
 * Analogue of {@link java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long,
 * java.util.concurrent.TimeUnit)}  }
 *
 * @author Sergii Kabashniuk
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduleRate {

    /**
     * @return - the time to delay first execution
     */
    long initialDelay() default 0;

    /**
     * @return the period between successive executions
     */
    long period() default 0;

    /**
     * @return the time unit of the initialDelay and period parameters
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * @return - name of configuration parameter for initialDelay
     */
    String initialDelayParameterName() default "";

    /**
     * @return - name of configuration parameter for period
     */
    String periodParameterName() default "";

}

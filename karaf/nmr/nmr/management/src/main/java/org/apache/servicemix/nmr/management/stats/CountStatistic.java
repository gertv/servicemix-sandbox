/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.nmr.management.stats;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A count statistic implementation
 *
 * @version $Revision: 564607 $
 */
public class CountStatistic extends Statistic {

    private final AtomicLong counter = new AtomicLong(0);
    private CountStatistic parent;

    public CountStatistic(CountStatistic parent, String name, String description) {
        this(name, description);
        this.parent = parent;
    }

    public CountStatistic(String name, String description) {
        this(name, "count", description);
    }

    public CountStatistic(String name, String unit, String description) {
        super(name, unit, description);
    }

    public void reset() {
        super.reset();
        counter.set(0);
    }

    public long getValue() {
        return counter.get();
    }

    public void updateValue(long count) {
        counter.set(count);
        updateUpdateCount();
    }

    public void add(long amount) {
        counter.addAndGet(amount);
        updateSampleTime();
        updateUpdateCount();
        if (parent != null) {
            parent.add(amount);
        }
    }

    public void increment() {
        counter.incrementAndGet();
        updateSampleTime();
        updateUpdateCount();
        if (parent != null) {
            parent.increment();
        }
    }

    public void subtract(long amount) {
        counter.addAndGet(-amount);
        updateSampleTime();
        updateUpdateCount();
        if (parent != null) {
            parent.subtract(amount);
        }
    }

    public void decrement() {
        counter.decrementAndGet();
        updateSampleTime();
        updateUpdateCount();
        if (parent != null) {
            parent.decrement();
        }
    }

    public CountStatistic getParent() {
        return parent;
    }

    public void setParent(CountStatistic parent) {
        this.parent = parent;
    }

    protected void appendFieldDescription(StringBuffer buffer) {
        buffer.append(" count: ");
        buffer.append(Long.toString(counter.get()));
        super.appendFieldDescription(buffer);
    }

    /**
     * @return the average time period that elapses between counter increments since the last reset.
     */
    public double getPeriod() {
        double count = counter.get();
        if (count == 0) {
            return 0;
        }
        double time = System.currentTimeMillis() - getStartTime();
        return time / (count * 1000.0);
    }

    /**
     * @return the number of times per second that the counter is incrementing since the last reset.
     */
    public double getFrequency() {
        double count = counter.get();
        double time = System.currentTimeMillis() - getStartTime();
        return count * 1000.0 / time;
    }

}

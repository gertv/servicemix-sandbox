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
package org.apache.servicemix.beanflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A useful base class for a activity which joins on the success of a collection
 * of child activities.
 * 
 * @version $Revision: $
 */
public abstract class JoinSupport extends TimeoutActivity {

    private List<Activity> children = new ArrayList<Activity>();
    private Set<Activity> toBeStarted = new HashSet();

    public JoinSupport() {
    }

    public JoinSupport(List<Activity> activities) {
        synchronized (children) {
            for (Activity activity : activities) {
                activity.getState().addRunnable(this);
                children.add(activity);
                toBeStarted.add(activity);
            }
        }
    }

    public JoinSupport(Activity... activities) {
        synchronized (children) {
            for (Activity activity : activities) {
                activity.getState().addRunnable(this);
                children.add(activity);
                toBeStarted.add(activity);
            }
        }
    }

    public void fork(Activity child) {
        synchronized (children) {
            child.getState().addRunnable(this);
            children.add(child);
            child.start();
        }
    }

    public void cancelFork(Activity child) {
        synchronized (children) {
            child.getState().removeRunnable(this);
            children.remove(child);
            child.stop();
        }
    }

    @Override
    protected void onValidStateChange() {
        int childCount = 0;
        int stoppedCount = 0;
        int failedCount = 0;
        synchronized (children) {
            childCount = children.size();
            for (Activity child : children) {
                if (child.isStopped()) {
                    stoppedCount++;
                    if (child.isFailed()) {
                        failedCount++;
                    }
                }
            }
        }
        onChildStateChange(childCount, stoppedCount, failedCount);
    }

    @Override
    protected void doStart() {
        super.doStart();

        // lets make sure that the child activities are started properly
        synchronized (children) {
            for (Activity child : toBeStarted) {
                child.start();
            }
            toBeStarted.clear();
        }
    }

    /**
     * Decide whether or not we are done based on the number of children, the
     * number of child activities stopped and the number of failed activities
     */
    protected abstract void onChildStateChange(int childCount, int stoppedCount, int failedCount);

}

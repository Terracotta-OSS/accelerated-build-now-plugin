package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.queue.QueueSorter;

import java.util.Collections;
import java.util.List;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowSorter extends QueueSorter {

  private final AbstractProject project;
  private final QueueSorter originalQueueSorter;
  private final AcceleratedBuildNowComparator comparator;

  public AcceleratedBuildNowSorter(AbstractProject project, QueueSorter originalQueueSorter) {
    this.project = project;
    this.originalQueueSorter =  originalQueueSorter;
    comparator = new AcceleratedBuildNowComparator(this.project);
  }

  @Override
  public void sortBuildableItems(List<Queue.BuildableItem> buildables) {
    if(this.originalQueueSorter != null) {
      this.originalQueueSorter.sortBuildableItems(buildables);
    }
    Collections.sort(buildables, comparator);
  }

  public QueueSorter getOriginalQueueSorter() {
    return originalQueueSorter;
  }

  public AbstractProject getProject() {
    return project;
  }
}

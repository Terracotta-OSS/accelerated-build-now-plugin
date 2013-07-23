package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.AbstractProject;
import hudson.model.Queue;

import java.util.Comparator;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowComparator implements Comparator<Queue.BuildableItem> {

  private final AbstractProject mostPriorityProject;

  public AcceleratedBuildNowComparator(AbstractProject mostPriorityProject) {
    this.mostPriorityProject = mostPriorityProject;
  }

  public int compare(Queue.BuildableItem buildableItem0, Queue.BuildableItem buildableItem1) {
    AbstractProject<?, ?> project0 = (AbstractProject<?, ?>) buildableItem0.task;
    AbstractProject<?, ?> project1 = (AbstractProject<?, ?>) buildableItem1.task;
    if(project0.equals(mostPriorityProject)) {
      return -1;
    }
    if(project1.equals(mostPriorityProject)) {
      return 1;
    }
    return 0;
  }

}
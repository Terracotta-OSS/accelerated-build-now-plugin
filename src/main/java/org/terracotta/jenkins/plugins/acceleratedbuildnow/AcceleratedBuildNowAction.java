package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.*;
import hudson.model.queue.QueueSorter;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowAction implements Action {

  private static final Logger LOG = Logger.getLogger(AcceleratedBuildNowAction.class.getName());
  private final AbstractProject project;

  public AcceleratedBuildNowAction(AbstractProject abstractProject) {
    this.project = abstractProject;
  }

  public String getDisplayName() {
    return "Accelerated Build Now !";
  }

  public String getIconFileName() {
    return "/plugin/accelerated-build-now-plugin/images/icon-64x64.jpg";
  }

  public String getUrlName() {
    return "accelerated";
  }
  public void doBuild(final StaplerRequest request, final StaplerResponse response) throws ServletException,
          IOException, InterruptedException {

    int lastBuildId = project.getLastBuild().getNumber();

    LOG.info("project : " + project.getName() + " needs to be built NOW !");

    //replace the original queue sorter with one that will place our project build first in the queue
    QueueSorter originalQueueSorter = Jenkins.getInstance().getQueue().getSorter();
    Jenkins.getInstance().getQueue().setSorter(new AcceleratedBuildNowSorter(project, originalQueueSorter));

    QueueTaskFuture b = project.scheduleBuild2(0, new Cause.UserCause(), new Action[0]);
    LOG.info("project : " + project.getName() + " is scheduled to build now !");


    // we sort the queue so that our project is next to be built on the list
    Jenkins.getInstance().getQueue().getSorter().sortBuildableItems(Jenkins.getInstance().getQueue().getBuildableItems());

    Label assignedLabel = project.getAssignedLabel();


    AbstractProject killedProject = null;
    List<AbstractProject> allItems = Jenkins.getInstance().getAllItems(AbstractProject.class);
    for (AbstractProject projectConsidered : allItems) {
      AbstractBuild lastBuild  = projectConsidered.getLastBuild();
      if(lastBuild.isBuilding()) {
        if(lastBuild.getCause(Cause.UserCause.class) == null && slaveRunningBuildCompatible(lastBuild,assignedLabel)) {
          LOG.info("project : " + lastBuild.getProject().getName() + " was not scheduled by a human, killing it right now to re schedule it later !");
          lastBuild.setResult(Result.ABORTED);
          killedProject = projectConsidered;
          break;
        }
      }
    }

    while(!project.isBuilding() && project.getLastBuild().getNumber() == lastBuildId ) {
      Thread.sleep(1000);
      LOG.info("project : " + project.getName() + " still not building !");
    }
    project.getLastBuild().getActions().add(new AcceleratedBuildNowBadgeAction());

    if(killedProject != null) {
      rescheduleKilledBuild(killedProject, new AcceleratedBuildNowKilledCause());
    }
    LOG.info("project : " + project.getName() + " is building !");
    response.sendRedirect(request.getContextPath() + '/' + project.getUrl());

  }

  private boolean slaveRunningBuildCompatible(AbstractBuild lastBuild, Label assignedLabel) {
    boolean contains = lastBuild.getBuiltOn().getAssignedLabels().contains(assignedLabel);
    if(!contains) {
      LOG.info("build : " + lastBuild.getNumber() + " of project "+ lastBuild.getProject().getName() + " is not running on node compatible with " + assignedLabel.getName());
    }
    return contains;
  }

  private void rescheduleKilledBuild(AbstractProject projectKilled, Cause cause) {
    //replace the original queue sorter with one that will place our project build first in the queue
    QueueSorter originalQueueSorter = Jenkins.getInstance().getQueue().getSorter();
    Jenkins.getInstance().getQueue().setSorter(new AcceleratedBuildNowSorter(project, originalQueueSorter));

    QueueTaskFuture b = projectKilled.scheduleBuild2(0, cause, new Action[0]);
    LOG.info("project that was killed : " + projectKilled.getName() + " is scheduled to build now !");

    // we sort the queue so that our project is next to be built on the list
    Jenkins.getInstance().getQueue().getSorter().sortBuildableItems(Jenkins.getInstance().getQueue().getBuildableItems());
  }


  public static class AcceleratedBuildNowKilledCause extends Cause {

    public AcceleratedBuildNowKilledCause(){
    }

    public String getShortDescription() {
      return "Started by AcceleratedBuildNow plugin";
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof AcceleratedBuildNowKilledCause;
    }

    @Override
    public int hashCode() {
      return 42;
    }
  }

}

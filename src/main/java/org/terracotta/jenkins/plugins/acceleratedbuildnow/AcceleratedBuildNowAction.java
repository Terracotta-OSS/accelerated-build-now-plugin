package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.maven.MavenBuild;
import hudson.model.*;
import hudson.model.queue.QueueSorter;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 *
 * This class contains the main logic of the plugin
 *
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
          IOException, InterruptedException, ExecutionException {

    LOG.info("project : " + project.getName() + " needs to be built NOW !");

    Label assignedLabel = project.getAssignedLabel();

    // what if the user clicks repeatedly on the link ?
    boolean alreadyTakenCareOf = project.getLastBuild() != null && project.getLastBuild().isBuilding() || queueSorterPriorityOn(project);

    // what if the queue is empty and all executors are already busy ?
    boolean jenkinsIsFreeToBuild = Jenkins.getInstance().getQueue().isEmpty() && atLeastOneExecutorIsIdle(assignedLabel);

    if(alreadyTakenCareOf || jenkinsIsFreeToBuild) {
      LOG.info("No need for AcceleratedBuildNow plugin (already building or empty queue with idle executors");
      project.scheduleBuild2(0, new Cause.UserIdCause(), new Action[0]);
      Jenkins.getInstance().getQueue().getSorter().sortBuildableItems(Jenkins.getInstance().getQueue().getBuildableItems());
      response.sendRedirect(request.getContextPath() + '/' + project.getUrl());
      return;
    }

    QueueTaskFuture queueTaskFuture = project.scheduleBuild2(0, new Cause.UserIdCause(), new Action[0]);
    LOG.info("project : " + project.getName() + " is scheduled to build now !");


    //replace the original queue sorter with one that will place our project build first in the queue
    QueueSorter originalQueueSorter = Jenkins.getInstance().getQueue().getSorter();
    AcceleratedBuildNowSorter acceleratedBuildNowSorter = new AcceleratedBuildNowSorter(project, originalQueueSorter);
    Jenkins.getInstance().getQueue().setSorter(acceleratedBuildNowSorter);
    // we sort the queue so that our project is next to be built on the list
    Jenkins.getInstance().getQueue().getSorter().sortBuildableItems(Jenkins.getInstance().getQueue().getBuildableItems());

    AbstractBuild killedBuild = null;
    List<AbstractProject> allItems = Jenkins.getInstance().getAllItems(AbstractProject.class);
    for (AbstractProject projectConsidered : allItems) {
      AbstractBuild lastBuild  = getLastBuild(projectConsidered);

      if(lastBuild != null && lastBuild.isBuilding()) {
        if(isBuildNotTriggeredByHuman(lastBuild) && slaveRunningBuildCompatible(lastBuild,assignedLabel)) {
          LOG.info("project : " + lastBuild.getProject().getName() + " #" + lastBuild.getNumber() +" was not scheduled by a human, killing it right now to re schedule it later !");
          Executor executor = getExecutor(lastBuild);
          executor.interrupt(Result.ABORTED);
          killedBuild = lastBuild;
          break;
        }
      }
    }

    if(killedBuild == null) {
      LOG.info("project : " + project.getName() + " could not be built : no way to build it now !");
    } else {
      AbstractBuild projectBuild = ((Future<AbstractBuild>) queueTaskFuture.getStartCondition()).get();
      LOG.info("build #" + projectBuild.getNumber() + " for " + project.getName() + " was launched successfully !");

      // we add a nice badge to the killer build
      projectBuild.getActions().add(new AcceleratedBuildNowBadgeAction(killedBuild));

      // we add a nice badge to the killeD build
      killedBuild.getActions().add(new AcceleratedBuildNowVictimBadgeAction(projectBuild));

      // we re schedule the build that got killed
      rescheduleKilledBuild(killedBuild, new AcceleratedBuildNowKilledCause(), originalQueueSorter);
    }
    Jenkins.getInstance().getQueue().setSorter(originalQueueSorter);

    response.sendRedirect(request.getContextPath() + '/' + project.getUrl());
  }

  private Executor getExecutor(AbstractBuild lastBuild) {
    Executor executorConsidered =  null;
    for (Executor executor : lastBuild.getBuiltOn().toComputer().getExecutors()) {
       if(lastBuild.equals(executor.getCurrentExecutable())){
         executorConsidered = executor;
       }
    }
    return executorConsidered;
  }

  private AbstractBuild getLastBuild(AbstractProject projectConsidered) {
    AbstractBuild lastBuild = projectConsidered.getLastBuild();
    // be careful that lastBuild can be a maven module building !
    if(lastBuild instanceof MavenBuild) {
      lastBuild = ((MavenBuild)lastBuild).getParentBuild();
    }
    return lastBuild;
  }

  private boolean atLeastOneExecutorIsIdle(Label assignedLabel) {

    int idleExecutors = 0;

    if(assignedLabel == null) {
      // no assignedLabel ? that's fine, let's find any idle executor
      // code based on Label.class code
      Set<Node> nodes = new HashSet<Node>();
      Jenkins h = Jenkins.getInstance();
        nodes.add(h);
      for (Node n : h.getNodes()) {
          nodes.add(n);
      }
      for (Node n : nodes) {
        Computer c = n.toComputer();
        if(c!=null && (c.isOnline() || c.isConnecting()) && c.isAcceptingTasks()) {
          idleExecutors += c.countIdle();
        }
      }
    } else {
      idleExecutors = assignedLabel.getIdleExecutors();
    }
    return idleExecutors > 0;
  }

  private boolean queueSorterPriorityOn(AbstractProject project) {
    QueueSorter originalQueueSorter = Jenkins.getInstance().getQueue().getSorter();
    if(originalQueueSorter instanceof AcceleratedBuildNowSorter) {
      if(((AcceleratedBuildNowSorter) originalQueueSorter).getProject().equals(project)) {
        return true;
      }
    }
    return false;
  }

  private boolean isBuildNotTriggeredByHuman(AbstractBuild lastBuild) {
    return lastBuild.getCause(Cause.UserIdCause.class) == null && lastBuild.getCause(Cause.UserCause.class) == null ;
  }

  private boolean slaveRunningBuildCompatible(AbstractBuild lastBuild, Label assignedLabel) {
    boolean contains = assignedLabel == null ? true : lastBuild.getBuiltOn().getAssignedLabels().contains(assignedLabel);
    if(!contains) {
      LOG.info("build : " + lastBuild.getNumber() + " of project "+ lastBuild.getProject().getName() + " is not running on node compatible with " + assignedLabel.getName());
    }
    return contains;
  }

  private void rescheduleKilledBuild(AbstractBuild killedBuild, Cause cause, QueueSorter originalQueueSorter) throws ExecutionException, InterruptedException {
    //replace the original queue sorter with one that will place our project build first in the queue
    AcceleratedBuildNowSorter acceleratedBuildNowSorter = new AcceleratedBuildNowSorter(project, originalQueueSorter);
    Jenkins.getInstance().getQueue().setSorter(acceleratedBuildNowSorter);

    QueueTaskFuture queueTaskFuture = killedBuild.getProject().scheduleBuild2(0, cause, new Action[0]);
    LOG.info("build that was killed : " + killedBuild.getProject().getName() + " #" + killedBuild.getNumber() +" is scheduled to build next !");

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

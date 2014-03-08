package org.terracotta.jenkins.plugins.acceleratedbuildnow.it;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.FreeStyleProject;

import java.util.List;
import java.util.concurrent.Future;

import hudson.model.Hudson;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SleepBuilder;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.terracotta.jenkins.plugins.acceleratedbuildnow.AcceleratedBuildNowAction;

public class CantAbortMatrixBuildTest extends HudsonTestCase {

  @Test
  @LocalData
  public void test_matrix_build() throws Exception {
    System.out.println("I have : " + Hudson.getInstance().getNumExecutors() + " executor(s) available");

    MatrixProject job1 = Hudson.getInstance().getAllItems(MatrixProject.class).get(0);
    assertEquals("matrixJob", job1.getName());

    job1.getBuildersList().add(new SleepBuilder(3000));
    Future<MatrixBuild> scheduleBuild2 = job1.scheduleBuild2(0);

    FreeStyleProject acceleratedJob = createFreeStyleProject("acceleratedJob");
    acceleratedJob.getBuildersList().add(new SleepBuilder(3000));

    AcceleratedBuildNowAction acceleratedBuildNowAction = new AcceleratedBuildNowAction(acceleratedJob);
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getContextPath()).thenReturn("");

    StaplerResponse response = mock(StaplerResponse.class);
    doNothing().when(response).sendRedirect(anyString());

    acceleratedBuildNowAction.doBuild(request, response);
    List<Job> allItems = Hudson.getInstance().getAllItems(Job.class);
    acceleratedBuildNowAction.doBuild(request, response);

    while (!Hudson.getInstance().getQueue().isEmpty()) {
      Thread.sleep(1000);
      System.out.println("Waiting for the queue to empty");
    }

    for (Job job : allItems) {
      while (job.isBuilding()) {
        System.out.println("Job " + job.getName() + " is still building !");
        Thread.sleep(500);
      }
    }


    MatrixBuild job1LastBuild = job1.getBuilds().getLastBuild();
    List<MatrixRun> exactRuns = job1LastBuild.getRuns();
    assertBuildStatus(Result.SUCCESS, job1LastBuild);

    assertEquals(1, acceleratedJob.getBuilds().size());
    FreeStyleBuild acceleratedJobOnlyBuild = acceleratedJob.getBuilds().getLastBuild();
    assertBuildStatus(Result.SUCCESS, acceleratedJobOnlyBuild);

    // job1LastBuild started before acceleratedJobOnlyBuild
    assertTrue(job1LastBuild.getTimeInMillis() < acceleratedJobOnlyBuild.getTimeInMillis());
  }

}

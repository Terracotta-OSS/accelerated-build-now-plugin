package org.terracotta.jenkins.plugins.acceleratedbuildnow.it;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import hudson.model.Job;

import java.util.List;

import hudson.model.Hudson;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SleepBuilder;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.terracotta.jenkins.plugins.acceleratedbuildnow.AcceleratedBuildNowAction;

public class NominalTest extends HudsonTestCase {

  @Test
  @LocalData
  public void test_start_job1_then_accelerated_built_job_should_cancel_job1_start_itself_and_reschedule_job1()
      throws Exception {

    System.out.println("I have : " + Hudson.getInstance().getNumExecutors() + " executor(s) available");

    FreeStyleProject job1 = createFreeStyleProject("job1");
    job1.getBuildersList().add(new SleepBuilder(3000));
    job1.scheduleBuild2(0);

    FreeStyleProject acceleratedJob = createFreeStyleProject("acceleratedJob");
    acceleratedJob.getBuildersList().add(new SleepBuilder(3000));

    AcceleratedBuildNowAction acceleratedBuildNowAction = new AcceleratedBuildNowAction(acceleratedJob);
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getContextPath()).thenReturn("");

    StaplerResponse response = mock(StaplerResponse.class);
    doNothing().when(response).sendRedirect(anyString());

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

    FreeStyleBuild job1firstBuild = job1.getBuilds().getFirstBuild();
    assertBuildStatus(Result.ABORTED, job1firstBuild);

    FreeStyleBuild job1lastBuild = job1.getBuilds().getLastBuild();
    assertBuildStatus(Result.SUCCESS, job1lastBuild);

    assertEquals(1, acceleratedJob.getBuilds().size());
    FreeStyleBuild acceleratedJobOnlyBuild = acceleratedJob.getBuilds().getLastBuild();
    assertBuildStatus(Result.SUCCESS, acceleratedJobOnlyBuild);

    // job1firstBuild started before acceleratedJobOnlyBuild that started before job1lastBuild
    assertTrue(job1firstBuild.getTimeInMillis() < acceleratedJobOnlyBuild.getTimeInMillis());
    assertTrue(acceleratedJobOnlyBuild.getTimeInMillis() < job1lastBuild.getTimeInMillis());
  }

}

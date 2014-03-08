package org.terracotta.jenkins.plugins.acceleratedbuildnow.it;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import hudson.model.Hudson;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SleepBuilder;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.terracotta.jenkins.plugins.acceleratedbuildnow.AcceleratedBuildNowAction;

public class CantAbortHumanBuildTest extends HudsonTestCase {


  @Test
  @LocalData
  public void test_dont_abort_build_if_started_by_user() throws Exception {
    System.out.println("I have : " + Hudson.getInstance().getNumExecutors() + " executor(s) available");

    FreeStyleProject job1 = Hudson.getInstance().getAllItems(FreeStyleProject.class).get(0);
    assertEquals("simpleJobWithParameters", job1.getName());
    job1.getBuildersList().add(new SleepBuilder(3000));

    httpPostBuildToHudson("job/simpleJobWithParameters/buildWithParameters", "stringParameterName=Value&.crumb=test");

    FreeStyleProject acceleratedJob = createFreeStyleProject("acceleratedJob");
    acceleratedJob.getBuildersList().add(new SleepBuilder(3000));

    AcceleratedBuildNowAction acceleratedBuildNowAction = new AcceleratedBuildNowAction(acceleratedJob);
    StaplerRequest request = mock(StaplerRequest.class);
    when(request.getContextPath()).thenReturn("");

    StaplerResponse response = mock(StaplerResponse.class);
    doNothing().when(response).sendRedirect(anyString());

    acceleratedBuildNowAction.doBuild(request, response);

    while (!Hudson.getInstance().getQueue().isEmpty()) {
      Thread.sleep(1000);
      System.out.println("Waiting for the queue to empty");
    }

    assertEquals(3, job1.getBuilds().size());
    FreeStyleBuild job1FirstBuild = job1.getBuilds().get(1);
    assertBuildStatus(Result.SUCCESS, job1FirstBuild);
    job1FirstBuild.getCauses();
    for (Cause cause : job1FirstBuild.getCauses()) {
      System.out.println("the cause is : " + cause.getShortDescription());
      System.out.println("the cause class is : " + cause.getClass().getName());
    }

    assertEquals(1, acceleratedJob.getBuilds().size());
    FreeStyleBuild acceleratedJobOnlyBuild = acceleratedJob.getBuilds().getLastBuild();
    assertBuildStatus(Result.SUCCESS, acceleratedJobOnlyBuild);

    // job1firstBuild started before acceleratedJobOnlyBuild
    assertTrue(job1FirstBuild.getTimeInMillis() < acceleratedJobOnlyBuild.getTimeInMillis());
  }

  private void httpPostBuildToHudson(String urlSuffix, String urlParameters) throws MalformedURLException,
  IOException, ProtocolException {
    String url = getURL() + urlSuffix;
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();
    int responseCode = con.getResponseCode();
    System.out.println("\nSending 'POST' request to URL : " + url);
    System.out.println("Post parameters : " + urlParameters);
    System.out.println("Response Code : " + responseCode);
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer testResponse = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      testResponse.append(inputLine);
    }
    in.close();
    // print result
    // System.out.println(testResponse.toString());
  }

}

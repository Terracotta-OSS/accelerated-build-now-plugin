package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.queue.QueueSorter;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowSorterTest {

  @Test
  public void shouldWrapOriginalSorterTest() {
    DummyQueueSorter originalQueueSorter = new DummyQueueSorter();
    AcceleratedBuildNowSorter acceleratedBuildNowSorter = new AcceleratedBuildNowSorter(getAbstractProject("myProject"), originalQueueSorter);
    acceleratedBuildNowSorter.sortBuildableItems(new ArrayList<Queue.BuildableItem>());
    assertTrue(originalQueueSorter.wasCalled());
  }

  @Test
  public void shouldNotWrapAcceleratedBuildNowSorterTest() {
    DummyQueueSorter originalQueueSorter = new DummyQueueSorter();
    AcceleratedBuildNowSorter acceleratedBuildNowSorter = new AcceleratedBuildNowSorter(getAbstractProject("myProject"), originalQueueSorter);
    AcceleratedBuildNowSorter acceleratedBuildNowSorter2 = new AcceleratedBuildNowSorter(getAbstractProject("myProject"), acceleratedBuildNowSorter);
    assertEquals(acceleratedBuildNowSorter, acceleratedBuildNowSorter2.getOriginalQueueSorter());
  }


  private AbstractProject getAbstractProject(final String importantProject) {
    AbstractProject abstractProject = Mockito.mock(AbstractProject.class);
    when(abstractProject.getName()).thenReturn(importantProject);
    return abstractProject;
  }

  private class DummyQueueSorter extends QueueSorter{
    private boolean wasCalled = false;

    @Override
    public void sortBuildableItems(List<Queue.BuildableItem> buildables) {
      wasCalled =  true;
    }

    public boolean wasCalled()  {
      return wasCalled;
    }

  }

}

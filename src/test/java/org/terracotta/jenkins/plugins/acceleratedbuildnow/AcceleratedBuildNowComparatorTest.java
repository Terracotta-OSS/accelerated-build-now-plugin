package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.*;
import hudson.model.Queue;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import static org.hamcrest.CoreMatchers.is;

import java.util.*;

import static org.mockito.Mockito.when;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowComparatorTest {


  @Test
  public void sortBuildItemsTest() {
    AbstractProject notImportant = getAbstractProject("notImportant");
    AbstractProject notImportantEither = getAbstractProject("notImportantEither");
    AbstractProject importantProject = getAbstractProject("importantProject");


    List<Queue.BuildableItem> buildableItemList =   new ArrayList<Queue.BuildableItem>();
    Queue.BuildableItem  notImportantBuildableItem =  new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(),notImportant,new ArrayList<Action>()));
    buildableItemList.add(notImportantBuildableItem);
    Queue.BuildableItem  notImportantEitherBuildableItem =  new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(),notImportantEither,new ArrayList<Action>()));
    buildableItemList.add(notImportantEitherBuildableItem);
    Queue.BuildableItem  importantBuildableItem =  new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(),importantProject,new ArrayList<Action>()));
    buildableItemList.add(importantBuildableItem);

    Assert.assertThat(buildableItemList.get(0), is(notImportantBuildableItem));
    Assert.assertThat(buildableItemList.get(1), is(notImportantEitherBuildableItem));
    Assert.assertThat(buildableItemList.get(2), is(importantBuildableItem));

    Comparator<Queue.BuildableItem> acceleratedBuildNowComparator = new AcceleratedBuildNowComparator(importantProject);
    Collections.sort(buildableItemList, acceleratedBuildNowComparator);

    Assert.assertThat(buildableItemList.get(0), is(importantBuildableItem));
    Assert.assertThat(buildableItemList.get(1), is(notImportantBuildableItem));
    Assert.assertThat(buildableItemList.get(2), is(notImportantEitherBuildableItem));
  }


  @Test
  public void sortBuildItemsTest2() {
    AbstractProject notImportant = getAbstractProject("notImportant");
    AbstractProject importantProject = getAbstractProject("importantProject");


    List<Queue.BuildableItem> buildableItemList =   new ArrayList<Queue.BuildableItem>();
    Queue.BuildableItem  importantBuildableItem =  new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(),importantProject,new ArrayList<Action>()));
    buildableItemList.add(importantBuildableItem);
    Queue.BuildableItem  notImportantBuildableItem =  new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(),notImportant,new ArrayList<Action>()));
    buildableItemList.add(notImportantBuildableItem);

    Assert.assertThat(buildableItemList.get(0), is(importantBuildableItem));
    Assert.assertThat(buildableItemList.get(1), is(notImportantBuildableItem));

    Comparator<Queue.BuildableItem> acceleratedBuildNowComparator = new AcceleratedBuildNowComparator(importantProject);
    Collections.sort(buildableItemList, acceleratedBuildNowComparator);

    Assert.assertThat(buildableItemList.get(0), is(importantBuildableItem));
    Assert.assertThat(buildableItemList.get(1), is(notImportantBuildableItem));
  }


  private AbstractProject getAbstractProject(final String importantProject) {
    AbstractProject abstractProject = Mockito.mock(AbstractProject.class);
    when(abstractProject.getName()).thenReturn(importantProject);
    return abstractProject;
  }

}

package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.*;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import jenkins.util.NonLocalizable;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.localizer.Localizable;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.Matchers.*;
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

    Assert.assertThat(buildableItemList,contains(notImportantBuildableItem, notImportantEitherBuildableItem, importantBuildableItem));

    Comparator<Queue.BuildableItem> acceleratedBuildNowComparator = new AcceleratedBuildNowComparator(importantProject);
    Collections.sort(buildableItemList, acceleratedBuildNowComparator);

    Assert.assertThat(buildableItemList,contains(importantBuildableItem, notImportantBuildableItem,notImportantEitherBuildableItem));
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

    Assert.assertThat(buildableItemList,contains(importantBuildableItem, notImportantBuildableItem));

    Comparator<Queue.BuildableItem> acceleratedBuildNowComparator = new AcceleratedBuildNowComparator(importantProject);
    Collections.sort(buildableItemList, acceleratedBuildNowComparator);

    Assert.assertThat(buildableItemList,contains(importantBuildableItem, notImportantBuildableItem));
  }


  private AbstractProject getAbstractProject(final String importantProject) {
    AbstractProject abstractProject = Mockito.mock(AbstractProject.class);
    when(abstractProject.getName()).thenReturn(importantProject);
    return abstractProject;
  }

}

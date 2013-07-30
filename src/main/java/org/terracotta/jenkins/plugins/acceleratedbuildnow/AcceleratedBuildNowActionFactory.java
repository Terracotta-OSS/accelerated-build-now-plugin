package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author : Anthony Dahanne
 */
@Extension
public class AcceleratedBuildNowActionFactory extends TransientProjectActionFactory{
  @Override
  public Collection<? extends Action> createFor(AbstractProject target) {
    ArrayList<Action> actions = new ArrayList<Action>();
    actions.add(new AcceleratedBuildNowAction(target));
    return actions;
  }
}

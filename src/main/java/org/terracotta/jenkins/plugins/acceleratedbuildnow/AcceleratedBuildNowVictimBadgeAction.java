package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.AbstractBuild;
import hudson.model.BuildBadgeAction;
import org.kohsuke.stapler.export.Exported;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowVictimBadgeAction implements BuildBadgeAction{
  private final static String ICON_PATH =  "/plugin/accelerated-build-now-plugin/images/icon-rotated-64x64.jpg";;
  private final AbstractBuild killerBuild;

  public AcceleratedBuildNowVictimBadgeAction(AbstractBuild killerBuild) {
    this.killerBuild =  killerBuild;
  }

  @Exported
  public String getIconPath() { return ICON_PATH; }

  @Exported
  public String getKillerBuildUrl() { return killerBuild.getProject().getUrl(); }

  @Exported
  public String getText() { return "This build was aborted and re scheduled by top priority build : " + killerBuild.getProject().getName() + " #" + killerBuild.getNumber(); }

  public String getDisplayName() {
    return "";
  }

  public String getIconFileName() {
    return "";
  }

  public String getUrlName() {
    return "";
  }

}

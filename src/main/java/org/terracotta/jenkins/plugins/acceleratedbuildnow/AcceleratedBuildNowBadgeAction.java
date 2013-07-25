package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.AbstractBuild;
import hudson.model.BuildBadgeAction;
import org.kohsuke.stapler.export.Exported;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowBadgeAction implements BuildBadgeAction{
  private final static String ICON_PATH =  "/plugin/accelerated-build-now-plugin/images/icon-64x64.jpg";;
  private final AbstractBuild killedBuild;

  public AcceleratedBuildNowBadgeAction(AbstractBuild killedBuild) {
    this.killedBuild =  killedBuild;
  }

  @Exported
  public String getIconPath() { return ICON_PATH; }

  @Exported
  public String getKilledBuildUrl() { return killedBuild.getProject().getUrl(); }

  @Exported
  public String getText() { return "This build was top priority built. It aborted and re scheduled " + killedBuild.getProject().getName() + " #" + killedBuild.getNumber(); }

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

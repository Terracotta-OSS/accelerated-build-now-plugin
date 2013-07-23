package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.model.BuildBadgeAction;
import org.kohsuke.stapler.export.Exported;

/**
 * @author : Anthony Dahanne
 */
public class AcceleratedBuildNowBadgeAction implements BuildBadgeAction{
  private final String iconPath =  "/plugin/accelerated-build-now-plugin/images/icon-64x64.jpg";;
  private final String text = "This build was top priority built thanks to Accelerated Build Now";

  @Exported
  public String getIconPath() { return iconPath; }

  @Exported
  public String getText() { return text; }

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

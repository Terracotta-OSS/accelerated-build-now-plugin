package org.terracotta.jenkins.plugins.acceleratedbuildnow;

import hudson.Plugin;

import java.util.logging.Logger;

/**
 * @author Anthony Dahanne
 */
public class AcceleratedBuildNowPlugin extends Plugin {
    private final static Logger LOG = Logger.getLogger(AcceleratedBuildNowPlugin.class.getName());

    public void start() throws Exception {
        LOG.info("Starting Accelerated Build Now Plugin");
    }
}


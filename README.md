Accelerated Build Now Plugin
============================
The Jenkins Accelerated Build Now Plugin allows Jenkins users to launch a project's build right away, even if the queue is long (moving it to the top of the queue) and even if no executor is available (killing and rescheduling builds not launched by "humans")

## How to build and test :
Simply clone this repo and run mvn clean install hpi:run

## How to install :
Download the archive named accelerated-build-now-plugin.hpi and use Jenkins installer advanced tab to upload and install it to Jenkins.

## How to use :
When your Jenkins cluster is overloaded with jobs (a queue with 10+ builds, all the executors busy with nightly builds that take ages), you know you will wait ages before the job you want to run effectively starts running.
Relax ! With the Accelerated Build Now Plugin, your job will run right away !

1. The queue is full of automatically (not user launched) scheduled jobs, and the only executor available is busy ...
![Screenshot](https://raw.github.com/Terracotta-OSS/accelerated-build-now-plugin/gh-pages/screenshots/queue_is_long.png "A long queue to wait for")

2. You want your job maven-surefire to run ASAP, so you click on the Accelerated Build Now button
![Screenshot](https://raw.github.com/Terracotta-OSS/accelerated-build-now-plugin/gh-pages/screenshots/accelerated_button.png "Accelerated Build Now !")

3. Your maven-surefire job just got priorized to the top of the queue and just started running (it had to kill the quartz job, but it rescheduled it already)
![Screenshot](https://raw.github.com/Terracotta-OSS/accelerated-build-now-plugin/gh-pages/screenshots/job_running.png "Your job is running")

4. A nice rhyno badge was added to your build that got "acceleratedly built" ; if you click on it you will see the killed build got a killed rhyno badge
![Screenshot](https://raw.github.com/Terracotta-OSS/accelerated-build-now-plugin/gh-pages/screenshots/build_prioritized "Killer Rhyno !")
![Screenshot](https://raw.github.com/Terracotta-OSS/accelerated-build-now-plugin/gh-pages/screenshots/build_aborted "Killed Rhyno !")

## How that works ?
When you click on the Accelerated Build Now button, the plugin will :
* make sure the queue is not empty and all the excutors are busy ( if not, it will normally schedule the build and exit)
* sort the queue using a QueueSorter wrapping any existing QueueSorter (such as the Priority Sorter Plugin queue sorter)
* look for any executors compatible with this job (checking labels) and running a job not scheduled by a "human" (SCM triggered, cron style triggered, etc..); if none is found, exit
* if a compatible executor is found: abort the build (and re schedule it for later) and mark it as killed by the plugin, wait for the accelerated build to start, mark it as accelerated

## Authors :
This plugin was developed by Terracotta, by

- [Anthony Dahanne](https://github.com/anthonydahanne/)

## License
Apache 2 licensed (see LICENSE.txt)

package com.zpw.myplayground.injkit;

import com.zpw.myplayground.LoggingKt;
import com.zpw.myplayground.injkit.internal.AndroidProjectFactory;
import com.zpw.myplayground.injkit.internal.LayoutStringExtractor;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class AndroidStringExtractorTask extends DefaultTask {

  private final LayoutStringExtractor layoutStringExtractor;

  public AndroidStringExtractorTask() {
    layoutStringExtractor = new LayoutStringExtractor(new AndroidProjectFactory());
  }

  @TaskAction
  public void extractStringsFromLayouts() throws Exception {
    String projectPath = getProject().getProjectDir().getPath();
    LoggingKt.log("projectPath is " + projectPath);
    layoutStringExtractor.extract(projectPath);
  }
}
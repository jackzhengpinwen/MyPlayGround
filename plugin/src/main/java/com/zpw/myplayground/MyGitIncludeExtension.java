package com.zpw.myplayground;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * The main configuration of the Git include plugin.
 */
public interface MyGitIncludeExtension {
    /**
     * Determines, in milliseconds, how often the repository should be updated.
     * By default, 24 hours.
     * @return the refresh interval property
     */
    Property<Long> getRefreshIntervalMillis();

    /**
     * Determine where the Git repositories should be checked out.
     * DirectoryProperty 由 Provider 提供，不需要具体实现
     * @return the directory property
     */
    DirectoryProperty getCheckoutsDirectory();

    /**
     * Includes a Git repository as a Gradle included build.
     * @param name the name of the included build
     * @param spec the configuration of the Git repository
     */
    void include(String name, Action<? super MyIncludedGitRepo> spec);
}

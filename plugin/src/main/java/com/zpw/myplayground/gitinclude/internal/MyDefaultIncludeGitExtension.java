/*
 * Copyright 2003-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zpw.myplayground.gitinclude.internal;

import com.zpw.myplayground.gitinclude.MyGitIncludeExtension;
import com.zpw.myplayground.gitinclude.MyIncludedGitRepo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import static com.zpw.myplayground.gitinclude.internal.ProviderUtils.forUseAtConfigurationTime;

public abstract class MyDefaultIncludeGitExtension implements MyGitIncludeExtension {
    private final static Logger LOGGER = LoggerFactory.getLogger(MyDefaultIncludeGitExtension.class);

    public static final String LOCAL_GIT_PREFIX = "local.git.";
    public static final String AUTO_GIT_DIRS = "auto.include.git.dirs";

    private final Settings settings;
    private Map<String, MyCheckoutMetadata> checkoutMetadata;

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Inject
    public MyDefaultIncludeGitExtension(Settings settings) {
        this.settings = settings;
    }

    /**
     * gitRepositories {
     *     include("circleimageview") {
     *         uri.set("https://github.com/hdodenhof/CircleImageView.git")
     *         branch.set("master")
     *     }
     * }
     * 在settings.gradle.kts中配置的时候就会调用include()方法，name 是 circleimageview ，
     * 方法体就是 spec.
     * @param name the name of the included build
     * @param spec the configuration of the Git repository
     */
    @Override
    public void include(String name, Action<? super MyIncludedGitRepo> spec) {
        System.out.println("MyDefaultIncludeGitExtension include");
        System.out.println("name is " + name + ", spec is " + spec);
        getCheckoutsDirectory().finalizeValue();
        ProviderFactory providers = getProviders();

        // 读取保存的数据
        readCheckoutMetadata();

        MyDefaultIncludedGitRepo repo = (MyDefaultIncludedGitRepo) getObjects()
                .newInstance(MyDefaultIncludedGitRepo.class, name);
        System.out.println("初始化 repo is " + repo);
        repo.getCheckoutDirectory()
                .convention(getCheckoutsDirectory().map(dir -> dir.dir(name)));
        spec.execute(repo);
        System.out.println("执行 spec 之后，repo is " + repo);
        File repoDir = repo.getCheckoutDirectory().get().getAsFile();
        String localRepoProperty = LOCAL_GIT_PREFIX + repo.getName();
        System.out.println("localRepoProperty is " + localRepoProperty);

        // 读取配置文件中自动加载的目录
        Provider<String> autoGitDirs;
        autoGitDirs = (Provider<String>) forUseAtConfigurationTime(providers.gradleProperty(AUTO_GIT_DIRS));
        Map<String, List<File>> autoDirs = Collections.emptyMap();
        if (autoGitDirs.isPresent()) {
            autoDirs = Arrays.stream(autoGitDirs.get().split("[,;](\\s)?"))
                    .map(File::new)
                    .flatMap(dir -> {
                                File[] dirEntries = dir.listFiles();
                                return dirEntries == null
                                        ? Stream.empty()
                                        : Arrays.stream(dirEntries)
                                        .filter(File::isDirectory);
                            }
                    )
                    .collect(Collectors.groupingBy(File::getName));
        } else {
            System.out.println("autoGitDirs is empty!");
        }

        // 读取本地设置的单目录仓库
        Map<String, List<File>> finalAutoDirs = autoDirs;
        Provider<String> localRepo = (Provider<String>) forUseAtConfigurationTime(providers.gradleProperty(localRepoProperty))
                .orElse(forUseAtConfigurationTime(providers.systemProperty(localRepoProperty)))
                .orElse(providers.provider(() -> {
                    List<File> files = finalAutoDirs.get(repo.getName());
                    if (files == null) {
                        return null;
                    }
                    if (files.size() == 1) {
                        return files.get(0).toString();
                    }
                    throw new GradleException("More than one directory named " + repo.getName() + " exists in auto Git repositories: " + files);
                }));
        if (localRepo.isPresent()) {
            System.out.println("Using local repository for " + localRepo.get() + " instead of cloning");
            repoDir = new File(localRepo.get());
        } else {
            System.out.println("CloneOrUpdate remote repository for " + repo.getName());
            cloneOrUpdate(repoDir, repo);
        }
        repo.configure(settings, repoDir);
    }

    private void cloneOrUpdate(File repoDir, MyIncludedGitRepo repo) {
        String uri = repo.getUri().get();
        String rev = repo.getCommit().getOrElse("");
        String branchOrTag = repo.getTag().orElse(repo.getBranch()).orElse("").get();
        MyCheckoutMetadata current = new MyCheckoutMetadata(uri, rev, branchOrTag, System.currentTimeMillis());
        System.out.println("checkoutMetadata is " + current);
        if (repoDir.exists() && new File(repoDir, ".git").exists()) {
            updateRepository(repoDir, uri, rev, branchOrTag, current);
        } else {
            cloneRepository(repoDir, uri, rev, branchOrTag, current);
        }
    }

    private void updateRepository(File repoDir, String uri, String rev, String branchOrTag, MyCheckoutMetadata current) {
        if (checkoutMetadata.containsKey(uri)) {
            MyCheckoutMetadata old = checkoutMetadata.get(uri);
            boolean sameRef = Objects.equals(current.getRef(), old.getRef());
            boolean sameBranch = current.getBranch().equals(old.getBranch());
            boolean upToDate = current.getLastUpdate() - old.getLastUpdate() < getRefreshIntervalMillis().get();
            if (sameRef && sameBranch && upToDate) {
                return;
            }
        }
        try (Git git = Git.open(repoDir)) {
            String fullBranch = git.getRepository().getFullBranch();
            if (fullBranch.startsWith("refs/heads/")) {
                LOGGER.info("Pulling from {}", uri);
                git.pull().call();
            }
            LOGGER.info("Checking out ref {} of {}", rev, uri);
            if (!rev.isEmpty()) {
                git.checkout()
                        .setName(rev)
                        .call();
            } else {
                Ref resolve = git.getRepository().findRef(branchOrTag);
                if (resolve == null) {
                    List<Ref> refs = git.getRepository().getRefDatabase().getRefs();
                    for (Ref ref : refs) {
                        if (ref.getName().endsWith(branchOrTag)) {
                            resolve = ref;
                            break;
                        }
                    }
                }
                if (resolve != null) {
                    git.checkout()
                            .setName(resolve.getName())
                            .call();
                } else {
                    throw new GradleException("Branch or tag " + branchOrTag + " not found");
                }
            }
        } catch (GitAPIException | IOException e) {
            throw new GradleException("Unable to update repository contents: " + e.getMessage(), e);
        } finally {
            checkoutMetadata.put(uri, current);
        }
    }

    private void cloneRepository(File repoDir, String uri, String rev, String branchOrTag, MyCheckoutMetadata current) {
        System.out.println("Checking out " + uri + " ref " + rev + " in " + repoDir);
        try {
            Git.cloneRepository()
                    .setURI(uri)
                    .setBranch(branchOrTag)
                    .setDirectory(repoDir)
                    .call();
            if (!rev.isEmpty()) {
                try (Git git = Git.open(repoDir)) {
                    git.checkout()
                            .setName(rev)
                            .call();
                }
            }
        } catch (GitAPIException | IOException e) {
            throw new GradleException("Unable to clone repository contents: " + e.getMessage(), e);
        } finally {
            checkoutMetadata.put(uri, current);
        }
    }

    private void readCheckoutMetadata() {
        System.out.println("readCheckoutMetadata is called!");
        if (checkoutMetadata == null) {
            checkoutMetadata = new HashMap<>();
            File metadataFile = getCheckoutsDirectory()
                    .file("checkouts.bin").get().getAsFile();
            if (metadataFile.exists()) {
                try(DataInputStream dis = new DataInputStream(new FileInputStream(metadataFile))) {
                    int size = dis.readInt();
                    for(int i = 0; i < size; i++) {
                        String uri = dis.readUTF();
                        String ref = dis.readUTF();
                        String branch = dis.readUTF();
                        Long lastUpdate = dis.readLong();
                        System.out.println(
                                "readCheckoutMetadata uri is " + uri
                                + "ref is" + ref
                                + "branch is " + branch
                                + "lastUpdate is " + lastUpdate
                        );
                        checkoutMetadata.put(uri, new MyCheckoutMetadata(uri, ref, branch, lastUpdate));
                    }
                } catch (IOException e) {
                    throw new GradleException("Unable to read checkout metadata", e);
                }
            }
        } else {
            System.out.println("checkoutMetadata is null");
        }
    }

    public void writeCheckoutMetadata() {
        System.out.println("writeCheckoutMetadata is called!");
        if (checkoutMetadata == null) {
            return;
        }
        File metadataFile = getCheckoutsDirectory()
                .file("checkouts.bin").get().getAsFile();
        File parentFile = metadataFile.getParentFile();
        parentFile.mkdirs();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(metadataFile))) {
            int size = checkoutMetadata.size();
            dos.writeInt(size);
            checkoutMetadata.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> {
                        MyCheckoutMetadata value = e.getValue();
                        try {
                            dos.writeUTF(value.getUri());
                            dos.writeUTF(value.getRef());
                            dos.writeUTF(value.getBranch());
                            dos.writeLong(value.getLastUpdate());
                            System.out.println(
                                    "writeCheckoutMetadata uri is " + value.getUri()
                                            + "ref is" + value.getRef()
                                            + "branch is " + value.getBranch()
                                            + "lastUpdate is " + value.getLastUpdate()
                            );
                        } catch (IOException ex) {
                            throw new GradleException("Unable to write checkout metadata", ex);
                        }
                    });
        } catch (IOException e) {
            throw new GradleException("Unable to write checkout metadata", e);
        }
    }
}

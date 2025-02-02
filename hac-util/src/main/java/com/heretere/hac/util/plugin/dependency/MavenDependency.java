/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.heretere.hac.util.plugin.dependency;

import com.heretere.hac.util.plugin.HACPlugin;
import com.heretere.hac.util.plugin.dependency.annotations.Maven;
import com.heretere.hac.util.plugin.dependency.relocation.annotations.Relocation;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * The type Maven dependency.
 */
public final class MavenDependency extends Dependency {
    /**
     * The group id.
     */
    private final @NotNull String groupId;
    /**
     * The artifact id.
     */
    private final @NotNull String artifactId;
    /**
     * The version.
     */
    private final @NotNull String version;
    /**
     * The repo url.
     */
    private final @NotNull String repoURL;

    /**
     * Instantiates a new Maven dependency.
     *
     * @param parent      the parent
     * @param groupId     the group id
     * @param artifactId  the artifact id
     * @param version     the version
     * @param repoURL     the repo url
     * @param relocations the relocations
     */
    public MavenDependency(
        final @NotNull HACPlugin parent,
        final @NotNull String groupId,
        final @NotNull String artifactId,
        final @NotNull String version,
        final @NotNull String repoURL,
        final @NotNull Set<Relocation> relocations
    ) {
        super(parent, relocations);
        this.groupId = StringUtils.replace(groupId, "|", ".");
        this.artifactId = artifactId;
        this.version = version;
        this.repoURL = repoURL + (StringUtils.endsWith(repoURL, "/") ? "" : "/");
    }

    /**
     * Instantiates a new Maven dependency.
     *
     * @param parent      the parent
     * @param maven       the maven
     * @param relocations the relocations
     */
    public MavenDependency(
        final @NotNull HACPlugin parent,
        final @NotNull Maven maven,
        final @NotNull Set<Relocation> relocations
    ) {
        this(parent, maven.groupId(), maven.artifactId(), maven.version(), maven.repoUrl(), relocations);
    }

    @Override
    public boolean needsDownload() {
        return !Files.exists(this.getDownloadLocation());
    }

    @Override
    public boolean needsRelocation() {
        return !Files.exists(this.getRelocatedLocation());
    }

    @Override
    public @NotNull Path getDownloadLocation() {
        return super.getParent().getBaseDirectory().resolve("dependencies").resolve(this.getName() + ".jar");
    }

    @Override
    public @NotNull Path getRelocatedLocation() {
        return super.getParent().getBaseDirectory().resolve("dependencies").resolve(this.getName() + "-relocated.jar");
    }

    @Override
    public @NotNull Optional<URL> getManualURL() {
        return this.getDownloadURL();
    }

    @Override
    public @NotNull Optional<URL> getDownloadURL() {
        URL url;

        try {
            url = new URL(String.format(
                "%s%s/%s/%s/%s-%s.jar",
                this.repoURL,
                this.groupId.replace(".", "/"),
                this.artifactId,
                this.version,
                this.artifactId,
                this.version
            ));
        } catch (MalformedURLException e) {
            url = null;
        }

        return Optional.ofNullable(url);
    }

    @Override
    public @NotNull String getName() {
        return this.artifactId + "-" + this.version;
    }
}

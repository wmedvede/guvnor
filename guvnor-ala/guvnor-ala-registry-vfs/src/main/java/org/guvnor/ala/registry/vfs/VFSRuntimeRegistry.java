/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.ala.registry.vfs;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.guvnor.ala.marshalling.JSONMarshaller;
import org.guvnor.ala.registry.impl.BaseRuntimeRegistry;
import org.guvnor.ala.runtime.Runtime;
import org.guvnor.ala.runtime.RuntimeId;
import org.guvnor.ala.runtime.providers.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;

/**
 * A runtime registry implementation based on the VFS.
 */
@ApplicationScoped
public class VFSRuntimeRegistry
        extends BaseRuntimeRegistry {

    private static final String PROVISIONING_PATH = "provisioning";

    private static final String RUNTIME_REGISTRY_PATH = "runtime-registry";

    private static final String PROVIDER_SUFFIX = "-provider.json";

    private static final String RUNTIME_SUFFIX = "-runtime.json";

    private static final Logger logger = LoggerFactory.getLogger(VFSRuntimeRegistry.class);

    private IOService ioService;

    private FileSystem fileSystem;

    /**
     * Root to the runtime registry files.
     */
    private org.uberfire.java.nio.file.Path registryRoot;

    private JSONMarshaller marshaller = new JSONMarshaller();

    public VFSRuntimeRegistry() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public VFSRuntimeRegistry(@Named("ioStrategy") final IOService ioService,
                              @Named("systemFS") final FileSystem fileSystem) {
        this.ioService = ioService;
        this.fileSystem = fileSystem;
    }

    @PostConstruct
    protected void init() {
        try {
            final Path fsRoot = fileSystem.getRootDirectories().iterator().next();
            Path provisioningPath = fsRoot.resolve(PROVISIONING_PATH);
            if (!ioService.exists(provisioningPath)) {
                provisioningPath = ioService.createDirectory(provisioningPath);
            }
            registryRoot = provisioningPath.resolve(RUNTIME_REGISTRY_PATH);
            if (!ioService.exists(registryRoot)) {
                registryRoot = ioService.createDirectory(registryRoot);
            }
        } catch (Exception e) {
            //uncommon error
            logger.error("An error was produced during " + VFSRuntimeRegistry.class.getName() +
                                 " directories initialization.",
                         e);
        }
        initializeRegistry();
    }

    @Override
    public void registerProvider(final Provider provider) {
        requireNonNull(provider,
                       "provider");
        final Path path = buildProviderPath(provider);
        final String marshalledProvider;
        final String content;
        try {
            marshalledProvider = marshaller.marshall(provider);
            VFSRegistryEntry entry = new VFSRegistryEntry(VFSRegistryEntry.JSON,
                                                          provider.getClass().getName(),
                                                          marshalledProvider);
            content = marshaller.marshall(entry);
        } catch (Exception e) {
            //uncommon error
            logger.error("Unexpected error was produced during provider marshalling, provider: " + provider,
                         e);
            throw new RuntimeException("Unexpected error was produced during provider marshalling, provider: " + provider,
                                       e);
        }

        try {
            ioService.startBatch(path.getFileSystem());
            ioService.write(path,
                            content);
        } finally {
            ioService.endBatch();
        }
        super.registerProvider(provider);
    }

    @Override
    public void deregisterProvider(final Provider provider) {
        final Path path = buildProviderPath(provider);
        try {
            ioService.startBatch(path.getFileSystem());
            ioService.deleteIfExists(path);
        } finally {
            ioService.endBatch();
        }
        super.deregisterProvider(provider);
    }

    @Override
    public void deregisterProvider(String providerId) {
        super.deregisterProvider(providerId);
    }

    @Override
    public void registerRuntime(Runtime runtime) {
        super.registerRuntime(runtime);
    }

    @Override
    public void deregisterRuntime(RuntimeId runtimeId) {
        super.deregisterRuntime(runtimeId);
    }

    /**
     * Initial startup of the registry by reading all the registered elements from the VFS.
     */
    protected void initializeRegistry() {
        final List<Provider> providers = readProviders();
        providers.forEach(super::registerProvider);
    }

    protected List<Provider> readProviders() {
        final List<Provider> providers = new ArrayList<>();
        Provider provider;
        VFSRegistryEntry entry;
        String content;
        for (Path path : ioService.newDirectoryStream(registryRoot)) {
            try {
                if (path.getFileName().toString().endsWith(PROVIDER_SUFFIX)) {
                    content = ioService.readAllString(path);
                    entry = marshaller.unmarshall(content,
                                                  VFSRegistryEntry.class);
                    provider = unmarshallProvider(entry);
                    providers.add(provider);
                }
            } catch (Exception e) {
                logger.error("An error was produced while processing provider entry for path: " + path,
                             e);
            }
        }
        return providers;
    }

    protected Provider unmarshallProvider(VFSRegistryEntry entry) throws Exception {
        final Class<?> clazz = Class.forName(entry.getContentType());
        return (Provider) marshaller.unmarshallSimpleType(entry.getContent(),
                                                          clazz);
    }

    protected VFSRegistryEntry unmarshallEntry(Path path) throws Exception {
        String content = ioService.readAllString(path);
        return marshaller.unmarshall(content,
                                     VFSRegistryEntry.class);
    }

    public static String md5Hex(String content) {
        if (content == null) {
            return null;
        }
        return DigestUtils.md5Hex(content);
    }

    protected Path buildProviderPath(final Provider provider) {
        return registryRoot.resolve(md5Hex(provider.getId()) + PROVIDER_SUFFIX);
    }

    public static class VFSRegistryEntry {

        public static final String JSON = "json";
        public static final String XML = "xml";

        private String contentFormat;

        private String contentType;

        private String content;

        public String getContentFormat() {
            return contentFormat;
        }

        public String getContentType() {
            return contentType;
        }

        public String getContent() {
            return content;
        }

        public VFSRegistryEntry() {
            //no args constructor for marshalling purposes.
        }

        public VFSRegistryEntry(final String contentFormat,
                                final String contentType,
                                final String content) {
            this.contentFormat = contentFormat;
            this.contentType = contentType;
            this.content = content;
        }
    }
}

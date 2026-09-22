package com.ecdat.backend.input;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry mapping ScanInputType to corresponding InputAdapter implementations.
 */
@Component
public class InputAdapterRegistry {

    private final Map<ScanInputType, InputAdapter> adapters = new EnumMap<>(ScanInputType.class);

    public InputAdapterRegistry() {
        // Register default supported adapters
        registerAdapter(new ZipInputAdapter());
        registerAdapter(new DirectoryInputAdapter());

        // Register Source File adapter
        registerAdapter(new InputAdapter() {
            private final SourceFileInputProcessor processor = new SourceFileInputProcessor();

            @Override
            public ScanInputType getInputType() {
                return ScanInputType.SOURCE_FILE;
            }

            @Override
            public boolean supports(ScanInputType type) {
                return type == ScanInputType.SOURCE_FILE;
            }

            @Override
            public ScanWorkspace prepareWorkspace(ScanRequest request) throws java.io.IOException {
                AnalysisInput input = AnalysisInput.forSourceFile(request.getArchiveFile(), request.getContext());
                input.setScopes(request.getScopes());
                return processor.process(input);
            }
        });

        // Register explicit unsupported adapters
        registerAdapter(UnsupportedInputAdapter.forGitRepository());
        registerAdapter(UnsupportedInputAdapter.forFiles());
        registerAdapter(UnsupportedInputAdapter.forJar());
        registerAdapter(UnsupportedInputAdapter.forClass());
        registerAdapter(UnsupportedInputAdapter.forConfiguration());
        registerAdapter(UnsupportedInputAdapter.forContainer());

        // Aliases
        adapters.put(ScanInputType.REPOSITORY_URL, adapters.get(ScanInputType.GIT_REPOSITORY));
        adapters.put(ScanInputType.CONFIGURATION_FILE, adapters.get(ScanInputType.CONFIGURATION));
        adapters.put(ScanInputType.BINARY_FILE, adapters.get(ScanInputType.JAR));
    }

    public InputAdapterRegistry(List<InputAdapter> customAdapters) {
        this();
        if (customAdapters != null) {
            for (InputAdapter adapter : customAdapters) {
                registerAdapter(adapter);
            }
        }
    }

    public void registerAdapter(InputAdapter adapter) {
        if (adapter != null && adapter.getInputType() != null) {
            adapters.put(adapter.getInputType(), adapter);
        }
    }

    /**
     * Retrieves the input adapter for the requested input type.
     *
     * @param type the scan input type
     * @return matching InputAdapter
     * @throws UnsupportedOperationException if no adapter is registered
     */
    public InputAdapter getAdapter(ScanInputType type) {
        if (type == null) {
            throw new IllegalArgumentException("Scan input type cannot be null.");
        }

        InputAdapter adapter = adapters.get(type);
        if (adapter == null) {
            throw new UnsupportedOperationException("No input adapter registered for type: " + type);
        }
        return adapter;
    }

    /**
     * Checks if the given input type has an active and supported adapter.
     */
    public boolean isSupported(ScanInputType type) {
        return type != null && type.isSupported() && adapters.containsKey(type);
    }
}

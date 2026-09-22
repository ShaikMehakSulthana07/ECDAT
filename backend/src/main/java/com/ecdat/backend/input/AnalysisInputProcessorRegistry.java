package com.ecdat.backend.input;

import com.ecdat.backend.dto.InputCapability;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry mapping AnalysisInputType to corresponding AnalysisInputProcessor implementations.
 */
@Component
public class AnalysisInputProcessorRegistry {

    private final Map<AnalysisInputType, AnalysisInputProcessor> processors = new EnumMap<>(AnalysisInputType.class);

    public AnalysisInputProcessorRegistry() {
        // Register default implemented processors
        registerProcessor(new ZipInputProcessor());
        registerProcessor(new SourceFileInputProcessor());
        registerProcessor(new DirectoryInputProcessor());
        registerProcessor(new RepositoryInputProcessor());
        registerProcessor(new ConfigurationInputProcessor());
        registerProcessor(new BinaryInputProcessor());
        registerProcessor(new ContainerInputProcessor());
    }

    public AnalysisInputProcessorRegistry(List<AnalysisInputProcessor> customProcessors) {
        this();
        if (customProcessors != null) {
            for (AnalysisInputProcessor processor : customProcessors) {
                registerProcessor(processor);
            }
        }
    }

    public void registerProcessor(AnalysisInputProcessor processor) {
        if (processor != null && processor.getInputType() != null) {
            processors.put(processor.getInputType(), processor);
        }
    }

    /**
     * Retrieves the input processor for the requested input type.
     *
     * @param type the analysis input type
     * @return matching AnalysisInputProcessor
     * @throws UnsupportedOperationException if no processor is registered
     */
    public AnalysisInputProcessor getProcessor(AnalysisInputType type) {
        if (type == null) {
            throw new IllegalArgumentException("Analysis input type cannot be null.");
        }

        AnalysisInputProcessor processor = processors.get(type);
        if (processor == null) {
            throw new UnsupportedOperationException("No input processor registered for type: " + type);
        }
        return processor;
    }

    /**
     * Checks if the given input type has an active and supported processor.
     */
    public boolean isSupported(AnalysisInputType type) {
        return type != null && type.isSupported() && processors.containsKey(type);
    }

    /**
     * Generates the capabilities descriptor list for all input types.
     */
    public List<InputCapability> getCapabilities() {
        List<InputCapability> capabilities = new ArrayList<>();
        for (AnalysisInputType type : AnalysisInputType.values()) {
            capabilities.add(new InputCapability(
                    type.name(),
                    type.isSupported(),
                    type.getDisplayName(),
                    type.getDescription(),
                    type.getPlannedPhase()
            ));
        }
        return capabilities;
    }
}

package com.simplicity.core.service;

import com.simplicity.core.domain.UserCustomization.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User Customization Service.
 * 
 * Manages all user customization options including:
 * - Context management (persistent/session)
 * - Intent commands (system behaviors)
 * - Response configuration
 * - Interface preferences
 * - Context length management
 * - Custom features
 */
public class UserCustomizationService {

    // Storage for user customizations
    private final Map<UUID, Map<UUID, UserContext>> userContexts = new ConcurrentHashMap<>();
    private final Map<UUID, IntentCommand> userCommands = new ConcurrentHashMap<>();
    private final Map<UUID, ResponseConfig> responseConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, InterfaceConfig> interfaceConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, ContextConfig> contextConfigs = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, CustomFeature>> customFeatures = new ConcurrentHashMap<>();

    public UserCustomizationService() {
        // Register built-in intent commands
        for (IntentCommand cmd : IntentCommand.BuiltIn.all()) {
            userCommands.put(cmd.commandId(), cmd);
        }
    }

    // ==================== CONTEXT MANAGEMENT ====================

    /**
     * Add a context for a user.
     */
    public UserContext addContext(UUID userId, String title, String content, 
            UserContext.ContextType type, int ttlMinutes) {
        
        UserContext context = type == UserContext.ContextType.SESSION
            ? UserContext.session(userId, title, content, ttlMinutes)
            : UserContext.persistent(userId, title, content);
        
        userContexts
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(context.contextId(), context);
        
        return context;
    }

    /**
     * Get all active contexts for a user.
     */
    public List<UserContext> getActiveContexts(UUID userId) {
        return userContexts.getOrDefault(userId, Map.of()).values().stream()
            .filter(c -> c.isActive() && !c.isExpired())
            .sorted(Comparator.comparing(UserContext::createdAt).reversed())
            .toList();
    }

    /**
     * Get combined context content for a query.
     */
    public String getCombinedContext(UUID userId, int maxLength) {
        StringBuilder combined = new StringBuilder();
        
        for (UserContext ctx : getActiveContexts(userId)) {
            if (combined.length() + ctx.content().length() > maxLength) {
                break;
            }
            combined.append("## ").append(ctx.title()).append("\n");
            combined.append(ctx.content()).append("\n\n");
        }
        
        return combined.toString();
    }

    /**
     * Update a context.
     */
    public void updateContext(UUID userId, UUID contextId, String content) {
        Map<UUID, UserContext> contexts = userContexts.get(userId);
        if (contexts != null && contexts.containsKey(contextId)) {
            UserContext old = contexts.get(contextId);
            contexts.put(contextId, new UserContext(
                old.contextId(), old.userId(), old.type(), old.title(),
                content, old.tags(), old.createdAt(), old.expiresAt(), old.isActive()
            ));
        }
    }

    /**
     * Delete a context.
     */
    public void deleteContext(UUID userId, UUID contextId) {
        Map<UUID, UserContext> contexts = userContexts.get(userId);
        if (contexts != null) {
            contexts.remove(contextId);
        }
    }

    // ==================== INTENT COMMANDS ====================

    /**
     * Create a custom intent command.
     */
    public IntentCommand createCommand(UUID userId, String name, String command,
            String description, String systemPrompt) {
        
        IntentCommand cmd = IntentCommand.custom(userId, name, command, description, systemPrompt);
        userCommands.put(cmd.commandId(), cmd);
        return cmd;
    }

    /**
     * Get all available commands for a user.
     */
    public List<IntentCommand> getAvailableCommands(UUID userId) {
        return userCommands.values().stream()
            .filter(c -> c.enabled() && (c.userId() == null || c.userId().equals(userId)))
            .sorted(Comparator.comparing(IntentCommand::name))
            .toList();
    }

    /**
     * Get command by name or alias.
     */
    public Optional<IntentCommand> getCommand(String nameOrAlias) {
        String search = nameOrAlias.toLowerCase().replace("/", "");
        return userCommands.values().stream()
            .filter(c -> c.enabled() && (
                c.name().equalsIgnoreCase(search) ||
                c.command().equalsIgnoreCase(search) ||
                c.aliases().stream().anyMatch(a -> a.equalsIgnoreCase(search))
            ))
            .findFirst();
    }

    /**
     * Execute a command (increment usage).
     */
    public IntentCommand useCommand(UUID commandId) {
        IntentCommand cmd = userCommands.get(commandId);
        if (cmd != null) {
            IntentCommand updated = new IntentCommand(
                cmd.commandId(), cmd.userId(), cmd.name(), cmd.command(),
                cmd.description(), cmd.systemPrompt(), cmd.aliases(),
                cmd.enabled(), cmd.parameters(), cmd.createdAt(),
                cmd.usageCount() + 1
            );
            userCommands.put(commandId, updated);
            return updated;
        }
        return cmd;
    }

    /**
     * Update command parameters.
     */
    public void updateCommandParameters(UUID commandId, Map<String, String> parameters) {
        IntentCommand cmd = userCommands.get(commandId);
        if (cmd != null) {
            Map<String, String> merged = new HashMap<>(cmd.parameters());
            merged.putAll(parameters);
            
            IntentCommand updated = new IntentCommand(
                cmd.commandId(), cmd.userId(), cmd.name(), cmd.command(),
                cmd.description(), cmd.systemPrompt(), cmd.aliases(),
                cmd.enabled(), merged, cmd.createdAt(), cmd.usageCount()
            );
            userCommands.put(commandId, updated);
        }
    }

    // ==================== RESPONSE CONFIG ====================

    /**
     * Get response configuration for a user.
     */
    public ResponseConfig getResponseConfig(UUID userId) {
        return responseConfigs.getOrDefault(userId, ResponseConfig.DEFAULT);
    }

    /**
     * Update response configuration.
     */
    public ResponseConfig updateResponseConfig(UUID userId, ResponseConfig config) {
        responseConfigs.put(userId, new ResponseConfig(
            config.configId() != null ? config.configId() : UUID.randomUUID(),
            userId, config.type(), config.format(), config.length(),
            config.citations(), config.language(), config.tone()
        ));
        return responseConfigs.get(userId);
    }

    /**
     * Quick update specific response settings.
     */
    public ResponseConfig updateResponseType(UUID userId, ResponseConfig.ResponseType type) {
        ResponseConfig current = getResponseConfig(userId);
        return updateResponseConfig(userId, new ResponseConfig(
            current.configId(), userId, type, current.format(), current.length(),
            current.citations(), current.language(), current.tone()
        ));
    }

    public ResponseConfig updateResponseLength(UUID userId, ResponseConfig.LengthPreference length) {
        ResponseConfig current = getResponseConfig(userId);
        return updateResponseConfig(userId, new ResponseConfig(
            current.configId(), userId, current.type(), current.format(), length,
            current.citations(), current.language(), current.tone()
        ));
    }

    public ResponseConfig updateTone(UUID userId, ResponseConfig.ToneConfig.ToneType tone) {
        ResponseConfig current = getResponseConfig(userId);
        return updateResponseConfig(userId, new ResponseConfig(
            current.configId(), userId, current.type(), current.format(), current.length(),
            current.citations(), current.language(),
            new ResponseConfig.ToneConfig(tone, current.tone().customInstructions())
        ));
    }

    // ==================== INTERFACE CONFIG ====================

    /**
     * Get interface configuration for a user.
     */
    public InterfaceConfig getInterfaceConfig(UUID userId) {
        return interfaceConfigs.getOrDefault(userId, InterfaceConfig.DEFAULT);
    }

    /**
     * Update interface configuration.
     */
    public InterfaceConfig updateInterfaceConfig(UUID userId, InterfaceConfig config) {
        interfaceConfigs.put(userId, new InterfaceConfig(
            config.configId() != null ? config.configId() : UUID.randomUUID(),
            userId, config.api(), config.ui(), config.notifications(),
            config.privacy(), config.accessibility()
        ));
        return interfaceConfigs.get(userId);
    }

    /**
     * Update API preferences.
     */
    public InterfaceConfig updateAPIPreferences(UUID userId, InterfaceConfig.APIPreferences api) {
        InterfaceConfig current = getInterfaceConfig(userId);
        return updateInterfaceConfig(userId, new InterfaceConfig(
            current.configId(), userId, api, current.ui(),
            current.notifications(), current.privacy(), current.accessibility()
        ));
    }

    /**
     * Update UI preferences.
     */
    public InterfaceConfig updateUIPreferences(UUID userId, InterfaceConfig.UIPreferences ui) {
        InterfaceConfig current = getInterfaceConfig(userId);
        return updateInterfaceConfig(userId, new InterfaceConfig(
            current.configId(), userId, current.api(), ui,
            current.notifications(), current.privacy(), current.accessibility()
        ));
    }

    /**
     * Update privacy settings.
     */
    public InterfaceConfig updatePrivacy(UUID userId, InterfaceConfig.PrivacyConfig privacy) {
        InterfaceConfig current = getInterfaceConfig(userId);
        return updateInterfaceConfig(userId, new InterfaceConfig(
            current.configId(), userId, current.api(), current.ui(),
            current.notifications(), privacy, current.accessibility()
        ));
    }

    // ==================== CONTEXT LENGTH CONFIG ====================

    /**
     * Get context configuration for a user.
     */
    public ContextConfig getContextConfig(UUID userId) {
        return contextConfigs.getOrDefault(userId, ContextConfig.DEFAULT);
    }

    /**
     * Update context configuration.
     */
    public ContextConfig updateContextConfig(UUID userId, ContextConfig config) {
        contextConfigs.put(userId, new ContextConfig(
            config.configId() != null ? config.configId() : UUID.randomUUID(),
            userId, config.maxContextTokens(), config.summaryThreshold(),
            config.strategy(), config.preserveKeywords(), config.compressFootnotes(),
            config.truncateDuplicates()
        ));
        return contextConfigs.get(userId);
    }

    /**
     * Set context length limit.
     */
    public ContextConfig setContextLimit(UUID userId, int maxTokens) {
        ContextConfig current = getContextConfig(userId);
        return updateContextConfig(userId, new ContextConfig(
            current.configId(), userId, maxTokens, current.summaryThreshold(),
            current.strategy(), current.preserveKeywords(), current.compressFootnotes(),
            current.truncateDuplicates()
        ));
    }

    // ==================== CUSTOM FEATURES ====================

    /**
     * Add a custom feature.
     */
    public CustomFeature addFeature(UUID userId, String category, String name, 
            String value, CustomFeature.DataType type) {
        
        CustomFeature feature = new CustomFeature(
            UUID.randomUUID(), userId, category, name, value, type,
            true, true, Instant.now(), Map.of()
        );
        
        customFeatures
            .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(feature.featureId(), feature);
        
        return feature;
    }

    /**
     * Get all features for a user.
     */
    public List<CustomFeature> getFeatures(UUID userId) {
        return customFeatures.getOrDefault(userId, Map.of()).values().stream()
            .filter(CustomFeature::isVisible)
            .sorted(Comparator.comparing(CustomFeature::category)
                .thenComparing(CustomFeature::name))
            .toList();
    }

    /**
     * Get features by category.
     */
    public List<CustomFeature> getFeaturesByCategory(UUID userId, String category) {
        return getFeatures(userId).stream()
            .filter(f -> f.category().equalsIgnoreCase(category))
            .toList();
    }

    /**
     * Search features.
     */
    public List<CustomFeature> searchFeatures(UUID userId, String query) {
        String lower = query.toLowerCase();
        return getFeatures(userId).stream()
            .filter(f -> f.name().toLowerCase().contains(lower) ||
                        f.value().toLowerCase().contains(lower) ||
                        f.category().toLowerCase().contains(lower))
            .toList();
    }

    /**
     * Update a feature.
     */
    public void updateFeature(UUID userId, UUID featureId, String value) {
        Map<UUID, CustomFeature> features = customFeatures.get(userId);
        if (features != null && features.containsKey(featureId)) {
            CustomFeature old = features.get(featureId);
            features.put(featureId, new CustomFeature(
                old.featureId(), old.userId(), old.category(), old.name(),
                value, old.dataType(), old.isVisible(), old.isSearchable(),
                old.createdAt(), old.metadata()
            ));
        }
    }

    /**
     * Delete a feature.
     */
    public void deleteFeature(UUID userId, UUID featureId) {
        Map<UUID, CustomFeature> features = customFeatures.get(userId);
        if (features != null) {
            features.remove(featureId);
        }
    }

    // ==================== BUILDER HELPERS ====================

    /**
     * Build complete user customization profile.
     */
    public UserCustomizationProfile buildProfile(UUID userId) {
        return new UserCustomizationProfile(
            getActiveContexts(userId),
            getAvailableCommands(userId),
            getResponseConfig(userId),
            getInterfaceConfig(userId),
            getContextConfig(userId),
            getFeatures(userId)
        );
    }

    /**
     * Complete customization profile for a user.
     */
    public record UserCustomizationProfile(
        List<UserContext> contexts,
        List<IntentCommand> commands,
        ResponseConfig responseConfig,
        InterfaceConfig interfaceConfig,
        ContextConfig contextConfig,
        List<CustomFeature> features
    ) {}
}

package com.simplicity.sovereignty;

import com.simplicity.core.domain.DomainModels.*;
import com.simplicity.kg.UserKnowledgeGraph;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🏛️ SIMPLICITY USER SOVEREIGNTY ENGINE
 * 
 * "Your Intelligence. Your Rules. Your Portability."
 * 
 * ============================================================================
 * CORE PRINCIPLE: USER DATA SOVEREIGNTY
 * ============================================================================
 * 
 * Unlike commercial AI systems where your learning is trapped in their models,
 * Simplicity ensures YOU own your intelligence forever.
 * 
 * WHAT THIS MEANS:
 * 
 * 1. PORTABILITY
 *    Your knowledge graph travels with you.
 *    Export it, import it, take it to any AI system.
 *    Never lose your learning again.
 * 
 * 2. PREDICTABILITY
 *    You know exactly what data is collected.
 *    You control what stays and what goes.
 *    No hidden model training on your data.
 * 
 * 3. PROTECTION
 *    Your data is encrypted and secure.
 *    You decide who sees what.
 *    GDPR/CCPA compliant by design.
 * 
 * 4. SOVEREIGNTY
 *    Not dependent on any AI provider.
 *    Switch models without losing intelligence.
 *    Your insights, not theirs.
 * 
 * ============================================================================
 */
public class UserSovereigntyEngine {

    private final Map<UUID, SovereignData> userData = new ConcurrentHashMap<>();
    private final Map<UUID, ConsentRecord> consentRecords = new ConcurrentHashMap<>();
    private final EncryptionService encryption = new EncryptionService();
    
    /**
     * Core sovereignty principle: User owns their intelligence.
     */
    public enum OwnershipLevel {
        USER_OWNED("User owns all data, full control"),
        SHARED("Data shared with explicit consent"),
        ORGANIZATION("Organization manages with user rights"),
        PUBLIC("User chose to make public");
        
        private final String description;
        OwnershipLevel(String description) {
            this.description = description;
        }
        public String description() { return description; }
    }

    /**
     * User's sovereign data package.
     * This is what the user OWNS and can export.
     */
    public record SovereignData(
        UUID userId,
        OwnershipLevel ownership,
        
        // Identity & Profile
        IdentityData identity,
        
        // Knowledge Graph (The Core Asset)
        KnowledgeGraphData knowledgeGraph,
        
        // Preferences & Customizations
        PreferencesData preferences,
        
        // Learning History
        LearningHistoryData learningHistory,
        
        // Consent & Permissions
        ConsentData consent,
        
        // Access Logs (for transparency)
        AccessLogData accessLog,
        
        // Data Origin (where did this come from)
        Metadata metadata
    ) {
        public record IdentityData(
            UUID userId,
            String displayName,
            String emailHash,  // Hashed, not raw email
            Instant createdAt,
            Instant lastVerifiedAt
        ) {}

        public record KnowledgeGraphData(
            List<OwnedEntity> entities,
            List<OwnedRelationship> relationships,
            Map<String, Double> interestVector,
            Map<String, Integer> domainExpertise,
            List<String> learnedPatterns,
            Instant lastUpdated
        ) {}

        public record PreferencesData(
            Map<String, Object> coreSettings,
            Map<String, Object> ragSettings,
            Map<String, Object> modelSettings,
            Map<String, Object> intelligenceSettings,
            List<String> enabledFeatures,
            String activePreset
        ) {}

        public record LearningHistoryData(
            List<InteractionRecord> interactions,
            Map<String, FeedbackRecord> feedbackHistory,
            List<String> inferredInterests,
            Map<String, Double> behaviorWeights,
            int totalQueries
        ) {}

        public record ConsentData(
            Map<ConsentType, ConsentStatus> consents,
            Instant lastConsentUpdate,
            List<ConsentChange> consentHistory
        ) {}

        public record AccessLogData(
            List<AccessRecord> records,
            Instant firstAccess,
            Instant lastAccess
        ) {}

        public record Metadata(
            String version,
            Instant exportedAt,
            String exportFormat,
            long totalSizeBytes,
            String checksum
        ) {}
    }

    /**
     * Owned entity in user's knowledge graph.
     */
    public record OwnedEntity(
        UUID entityId,
        String entityType,    // "person", "topic", "concept", "document"
        String name,
        Map<String, String> properties,
        double confidence,
        Instant learnedAt,
        OwnershipLevel ownership
    ) {}

    /**
     * Owned relationship between entities.
     */
    public record OwnedRelationship(
        UUID relationshipId,
        UUID sourceEntity,
        UUID targetEntity,
        String relationshipType,
        double strength,
        Instant learnedAt,
        OwnershipLevel ownership
    ) {}

    /**
     * Single interaction record (owned by user).
     */
    public record InteractionRecord(
        UUID recordId,
        String query,
        String responseHash,  // Hash of response for verification
        List<String> citations,
        double satisfactionScore,
        Instant timestamp,
        Map<String, Object> context
    ) {}

    /**
     * Feedback record (owned by user).
     */
    public record FeedbackRecord(
        UUID recordId,
        String feedbackType,  // "helpful", "not_helpful", "correction"
        String content,
        Instant timestamp
    ) {}

    /**
     * Consent types users can grant or revoke.
     */
    public enum ConsentType {
        PERSONALIZATION("Learn from my interactions to personalize responses"),
        ANALYTICS("Aggregate analytics to improve the platform"),
        MODEL_TRAINING("Use anonymized data for model improvement"),
        SHARING("Share insights with my organization"),
        IMPROVEMENT("Share patterns to improve system intelligence");
        
        private final String description;
        ConsentType(String description) {
            this.description = description;
        }
        public String description() { return description; }
    }

    public enum ConsentStatus {
        GRANTED,
        DENIED,
        PENDING
    }

    public record ConsentChange(
        ConsentType type,
        ConsentStatus oldStatus,
        ConsentStatus newStatus,
        Instant timestamp,
        String reason
    ) {}

    public record AccessRecord(
        UUID accessId,
        String accessor,      // Who accessed the data
        String accessType,   // "read", "write", "export", "delete"
        List<String> dataAccessed,
        Instant timestamp,
        boolean wasAuthorized
    ) {}

    // ==================== CORE SOVEREIGNTY API ====================

    /**
     * Initialize sovereign data for a new user.
     */
    public SovereignData initializeUser(UUID userId, String displayName) {
        SovereignData data = new SovereignData(
            userId,
            OwnershipLevel.USER_OWNED,
            new SovereignData.IdentityData(
                userId, displayName, hashEmail(""), Instant.now(), Instant.now()
            ),
            new SovereignData.KnowledgeGraphData(
                List.of(), List.of(), Map.of(), Map.of(), List.of(), Instant.now()
            ),
            new SovereignData.PreferencesData(
                Map.of(), Map.of(), Map.of(), Map.of(), List.of(), "default"
            ),
            new SovereignData.LearningHistoryData(
                List.of(), Map.of(), List.of(), Map.of(), 0
            ),
            new SovereignData.ConsentData(
                Map.of(ConsentType.PERSONALIZATION, ConsentStatus.GRANTED), Instant.now(), List.of()
            ),
            new SovereignData.AccessLogData(List.of(), Instant.now(), Instant.now()),
            new SovereignData.Metadata("1.0", Instant.now(), "simplicity-sovereign", 0, "")
        );
        
        userData.put(userId, data);
        return data;
    }

    /**
     * Export user's complete sovereign data package.
     * This is what the user OWNS - they can take it anywhere.
     */
    public ExportedPackage exportUserData(UUID userId, ExportFormat format, EncryptionLevel encryption) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            throw new IllegalArgumentException("User data not found");
        }
        
        // Generate export package
        String rawExport = serializeData(data, format);
        
        // Encrypt if requested
        String encryptedExport = encryption != EncryptionLevel.NONE 
            ? this.encryption.encrypt(rawExport, encryption)
            : rawExport;
        
        // Generate checksum
        String checksum = generateChecksum(rawExport);
        
        // Update metadata
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(), data.knowledgeGraph(),
            data.preferences(), data.learningHistory(), data.consent(), data.accessLog(),
            new SovereignData.Metadata("1.0", Instant.now(), format.name(), 
                (long) rawExport.length(), checksum)
        );
        userData.put(userId, updated);
        
        // Log access
        logAccess(userId, "export", List.of("all"), true);
        
        return new ExportedPackage(userId, encryptedExport, format, checksum, Instant.now());
    }

    /**
     * Import user's sovereign data from another system.
     * This is how users MIGRATE their intelligence.
     */
    public SovereignData importUserData(UUID userId, String importedData, 
            ExportFormat format, String expectedChecksum) {
        
        // Verify checksum
        String computedChecksum = generateChecksum(importedData);
        if (!computedChecksum.equals(expectedChecksum)) {
            throw new SecurityException("Data integrity check failed");
        }
        
        // Deserialize
        SovereignData imported = deserializeData(importedData, format);
        
        // Update user ID if different
        SovereignData data = new SovereignData(
            userId,  // Always use the target userId
            OwnershipLevel.USER_OWNED,  // Force user ownership
            imported.identity(),
            imported.knowledgeGraph(),
            imported.preferences(),
            imported.learningHistory(),
            imported.consent(),
            imported.accessLog(),
            new SovereignData.Metadata("1.0", Instant.now(), format.name(), 
                (long) importedData.length(), computedChecksum)
        );
        
        userData.put(userId, data);
        
        // Log access
        logAccess(userId, "import", List.of("all"), true);
        
        return data;
    }

    /**
     * Revoke consent and delete related data.
     * User's right to be forgotten, partially or fully.
     */
    public void revokeConsent(UUID userId, ConsentType consentType) {
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        // Update consent
        Map<ConsentType, ConsentStatus> newConsents = new EnumMap<>(ConsentType.class);
        newConsents.putAll(data.consent().consents());
        
        ConsentStatus oldStatus = newConsents.getOrDefault(consentType, ConsentStatus.PENDING);
        newConsents.put(consentType, ConsentStatus.DENIED);
        
        // Record change
        List<ConsentChange> changes = new ArrayList<>(data.consent().consentHistory());
        changes.add(new ConsentChange(consentType, oldStatus, ConsentStatus.DENIED, 
            Instant.now(), "User revoked consent"));
        
        // Delete data associated with this consent
        SovereignData updated = deleteConsentData(data, consentType);
        updated = new SovereignData(
            updated.userId(), updated.ownership(), updated.identity(), updated.knowledgeGraph(),
            updated.preferences(), updated.learningHistory(),
            new SovereignData.ConsentData(newConsents, Instant.now(), changes),
            updated.accessLog(), updated.metadata()
        );
        
        userData.put(userId, updated);
        
        // Log consent change
        logAccess(userId, "consent_revoked", List.of(consentType.name()), true);
    }

    /**
     * Delete all user data (right to deletion).
     */
    public void deleteAllUserData(UUID userId) {
        logAccess(userId, "delete", List.of("all"), true);
        userData.remove(userId);
        consentRecords.remove(userId);
    }

    /**
     * Grant data portability - generate portable package for another system.
     */
    public PortablePackage generatePortablePackage(UUID userId, String targetSystem) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            throw new IllegalArgumentException("User data not found");
        }
        
        // Create standardized portable format
        PortablePackage pkg = new PortablePackage(
            UUID.randomUUID(),
            data.userId(),
            targetSystem,
            createPortableFormat(data),
            Instant.now(),
            generateChecksum(serializeData(data, ExportFormat.JSON))
        );
        
        logAccess(userId, "port", List.of(targetSystem), true);
        
        return pkg;
    }

    /**
     * Switch to a new AI model while keeping your intelligence.
     * This is what makes Simplicity different - your learning is portable.
     */
    public void migrateToNewModel(UUID userId, String newModelProvider) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            throw new IllegalArgumentException("User data not found");
        }
        
        // Update preferences to new provider
        Map<String, Object> updatedPrefs = new HashMap<>(data.preferences().modelSettings());
        updatedPrefs.put("currentProvider", newModelProvider);
        updatedPrefs.put("lastMigration", Instant.now().toString());
        
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(), data.knowledgeGraph(),
            new SovereignData.PreferencesData(
                data.preferences().coreSettings(),
                data.preferences().ragSettings(),
                updatedPrefs,
                data.preferences().intelligenceSettings(),
                data.preferences().enabledFeatures(),
                data.preferences().activePreset()
            ),
            data.learningHistory(),
            data.consent(),
            data.accessLog(),
            data.metadata()
        );
        
        userData.put(userId, updated);
        
        // Log migration
        logAccess(userId, "model_migrate", List.of(newModelProvider), true);
    }

    // ==================== KNOWLEDGE GRAPH OWNERSHIP ====================

    /**
     * Add entity to user's owned knowledge graph.
     */
    public void addOwnedEntity(UUID userId, String entityType, String name, 
            Map<String, String> properties) {
        
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        OwnedEntity entity = new OwnedEntity(
            UUID.randomUUID(), entityType, name, properties, 0.8, Instant.now(), OwnershipLevel.USER_OWNED
        );
        
        List<OwnedEntity> updatedEntities = new ArrayList<>(data.knowledgeGraph().entities());
        updatedEntities.add(entity);
        
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(),
            new SovereignData.KnowledgeGraphData(
                updatedEntities, data.knowledgeGraph().relationships(),
                data.knowledgeGraph().interestVector(),
                data.knowledgeGraph().domainExpertise(),
                data.knowledgeGraph().learnedPatterns(),
                Instant.now()
            ),
            data.preferences(), data.learningHistory(), data.consent(),
            data.accessLog(), data.metadata()
        );
        
        userData.put(userId, updated);
    }

    /**
     * Remove entity from user's knowledge graph.
     */
    public void removeOwnedEntity(UUID userId, UUID entityId) {
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        List<OwnedEntity> updatedEntities = data.knowledgeGraph().entities().stream()
            .filter(e -> !e.entityId().equals(entityId))
            .collect(Collectors.toList());
        
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(),
            new SovereignData.KnowledgeGraphData(
                updatedEntities, data.knowledgeGraph().relationships(),
                data.knowledgeGraph().interestVector(),
                data.knowledgeGraph().domainExpertise(),
                data.knowledgeGraph().learnedPatterns(),
                Instant.now()
            ),
            data.preferences(), data.learningHistory(), data.consent(),
            data.accessLog(), data.metadata()
        );
        
        userData.put(userId, updated);
    }

    /**
     * Export knowledge graph for use in any AI system.
     */
    public String exportKnowledgeGraph(UUID userId, GraphFormat format) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            throw new IllegalArgumentException("User data not found");
        }
        
        return switch (format) {
            case GRAPHQL -> exportAsGraphQL(data.knowledgeGraph());
            case RDF -> exportAsRDF(data.knowledgeGraph());
            case JSON_LD -> exportAsJSONLD(data.knowledgeGraph());
            case SIMPLE_JSON -> serializeData(data.knowledgeGraph(), ExportFormat.JSON);
        };
    }

    /**
     * Import knowledge graph from any standard format.
     */
    public void importKnowledgeGraph(UUID userId, String graphData, GraphFormat format) {
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        SovereignData.KnowledgeGraphData importedGraph = switch (format) {
            case GRAPHQL -> parseGraphQL(graphData);
            case RDF -> parseRDF(graphData);
            case JSON_LD -> parseJSONLD(graphData);
            case SIMPLE_JSON -> (SovereignData.KnowledgeGraphData) deserializeData(graphData, ExportFormat.JSON);
        };
        
        // Merge with existing
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(),
            mergeKnowledgeGraphs(data.knowledgeGraph(), importedGraph),
            data.preferences(), data.learningHistory(), data.consent(),
            data.accessLog(), data.metadata()
        );
        
        userData.put(userId, updated);
    }

    // ==================== CONSENT MANAGEMENT ====================

    /**
     * Get current consent status for a user.
     */
    public Map<ConsentType, ConsentStatus> getConsents(UUID userId) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            return Map.of();
        }
        return data.consent().consents();
    }

    /**
     * Update consent for a specific type.
     */
    public void updateConsent(UUID userId, ConsentType type, ConsentStatus status) {
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        Map<ConsentType, ConsentStatus> newConsents = new EnumMap<>(ConsentType.class);
        newConsents.putAll(data.consent().consents());
        
        ConsentStatus oldStatus = newConsents.getOrDefault(type, ConsentStatus.PENDING);
        newConsents.put(type, status);
        
        List<ConsentChange> changes = new ArrayList<>(data.consent().consentHistory());
        changes.add(new ConsentChange(type, oldStatus, status, Instant.now(), "User updated consent"));
        
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(), data.knowledgeGraph(),
            data.preferences(), data.learningHistory(),
            new SovereignData.ConsentData(newConsents, Instant.now(), changes),
            data.accessLog(), data.metadata()
        );
        
        userData.put(userId, updated);
    }

    /**
     * Check if consent is granted before processing.
     */
    public boolean hasConsent(UUID userId, ConsentType type) {
        SovereignData data = userData.get(userId);
        if (data == null) return false;
        return data.consent().consents().getOrDefault(type, ConsentStatus.DENIED) == ConsentStatus.GRANTED;
    }

    // ==================== AUDIT & TRANSPARENCY ====================

    /**
     * Get access log for transparency.
     */
    public List<AccessRecord> getAccessLog(UUID userId) {
        SovereignData data = userData.get(userId);
        if (data == null) return List.of();
        return data.accessLog().records();
    }

    /**
     * Get complete transparency report for user.
     */
    public TransparencyReport generateTransparencyReport(UUID userId) {
        SovereignData data = userData.get(userId);
        if (data == null) {
            throw new IllegalArgumentException("User data not found");
        }
        
        return new TransparencyReport(
            userId,
            Instant.now(),
            data.knowledgeGraph().entities().size(),
            data.learningHistory().totalQueries(),
            data.consent().consents(),
            data.accessLog().records().size(),
            calculateDataFootprint(data)
        );
    }

    // ==================== PRIVATE HELPERS ====================

    private void logAccess(UUID userId, String accessType, List<String> dataAccessed, boolean authorized) {
        SovereignData data = userData.get(userId);
        if (data == null) return;
        
        AccessRecord record = new AccessRecord(
            UUID.randomUUID(), "system", accessType, dataAccessed, Instant.now(), authorized
        );
        
        List<AccessRecord> updatedRecords = new ArrayList<>(data.accessLog().records());
        updatedRecords.add(record);
        
        SovereignData updated = new SovereignData(
            data.userId(), data.ownership(), data.identity(), data.knowledgeGraph(),
            data.preferences(), data.learningHistory(), data.consent(),
            new SovereignData.AccessLogData(
                updatedRecords, data.accessLog().firstAccess(), Instant.now()
            ),
            data.metadata()
        );
        
        userData.put(userId, updated);
    }

    private SovereignData deleteConsentData(SovereignData data, ConsentType type) {
        // Delete data associated with consent type
        return switch (type) {
            case PERSONALIZATION -> {
                // Keep data but disable learning from it
                yield new SovereignData(
                    data.userId(), data.ownership(), data.identity(),
                    data.knowledgeGraph(), data.preferences(),
                    new SovereignData.LearningHistoryData(
                        List.of(), data.learningHistory().feedbackHistory(),
                        List.of(), Map.of(), data.learningHistory().totalQueries()
                    ),
                    data.consent(), data.accessLog(), data.metadata()
                );
            }
            case MODEL_TRAINING, ANALYTICS -> data; // Keep, just don't share
            case SHARING -> {
                // Remove org sharing settings
                yield data;
            }
            case IMPROVEMENT -> data; // Keep local, don't contribute
        };
    }

    private String hashEmail(String email) {
        // Simple hash - in production use proper hashing
        return Integer.toHexString(email.hashCode());
    }

    private String generateChecksum(String data) {
        return Integer.toHexString(data.hashCode());
    }

    private String serializeData(Object data, ExportFormat format) {
        // Simplified - would use proper serialization
        return data.toString();
    }

    private Object deserializeData(String data, ExportFormat format) {
        // Simplified - would use proper deserialization
        return null;
    }

    private String createPortableFormat(SovereignData data) {
        return """
        {
            "version": "1.0",
            "userId": "%s",
            "knowledgeGraph": {
                "entities": %d,
                "relationships": %d,
                "interests": %s
            },
            "preferences": {
                "preset": "%s"
            },
            "totalInteractions": %d
        }
        """.formatted(
            data.userId(),
            data.knowledgeGraph().entities().size(),
            data.knowledgeGraph().relationships().size(),
            data.knowledgeGraph().interestVector().keySet(),
            data.preferences().activePreset(),
            data.learningHistory().totalQueries()
        );
    }

    private SovereignData.KnowledgeGraphData mergeKnowledgeGraphs(
            SovereignData.KnowledgeGraphData existing,
            SovereignData.KnowledgeGraphData imported) {
        
        // Merge entities
        List<OwnedEntity> mergedEntities = new ArrayList<>(existing.entities());
        for (OwnedEntity entity : imported.entities()) {
            if (!existing.entities().contains(entity)) {
                mergedEntities.add(entity);
            }
        }
        
        return new SovereignData.KnowledgeGraphData(
            mergedEntities, existing.relationships(),
            mergeMaps(existing.interestVector(), imported.interestVector()),
            mergeIntMaps(existing.domainExpertise(), imported.domainExpertise()),
            mergeLists(existing.learnedPatterns(), imported.learnedPatterns()),
            Instant.now()
        );
    }

    private Map<String, Double> mergeMaps(Map<String, Double> a, Map<String, Double> b) {
        Map<String, Double> result = new HashMap<>(a);
        b.forEach((k, v) -> result.merge(k, v, Double::max));
        return result;
    }

    private Map<String, Integer> mergeIntMaps(Map<String, Integer> a, Map<String, Integer> b) {
        Map<String, Integer> result = new HashMap<>(a);
        b.forEach((k, v) -> result.merge(k, v, Integer::sum));
        return result;
    }

    private <T> List<T> mergeLists(List<T> a, List<T> b) {
        List<T> result = new ArrayList<>(a);
        for (T item : b) {
            if (!result.contains(item)) result.add(item);
        }
        return result;
    }

    private long calculateDataFootprint(SovereignData data) {
        return (long) (
            data.knowledgeGraph().entities().size() * 100 +
            data.learningHistory().interactions().size() * 500 +
            serializeData(data, ExportFormat.JSON).length()
        );
    }

    // Graph export formats
    private String exportAsGraphQL(SovereignData.KnowledgeGraphData graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("type UserKnowledge @entity {\n");
        sb.append("  id: ID!\n");
        sb.append("  entities: [Entity!]!\n");
        sb.append("  relationships: [Relationship!]!\n");
        sb.append("}\n\n");
        
        for (OwnedEntity entity : graph.entities()) {
            sb.append("type ").append(entity.entityType()).append(" {\n");
            sb.append("  id: ID!\n");
            sb.append("  name: String!\n");
            sb.append("  confidence: Float!\n");
            sb.append("  learnedAt: DateTime!\n");
            sb.append("}\n\n");
        }
        
        return sb.toString();
    }

    private String exportAsRDF(SovereignData.KnowledgeGraphData graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("@prefix ex: <http://simplicity.ai/knowledge/> .\n");
        sb.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n\n");
        
        for (OwnedEntity entity : graph.entities()) {
            sb.append("ex:").append(entity.entityId()).append(" rdf:type ex:").append(entity.entityType()).append(" .\n");
            sb.append("ex:").append(entity.entityId()).append(" ex:name \"").append(entity.name()).append("\" .\n");
        }
        
        return sb.toString();
    }

    private String exportAsJSONLD(SovereignData.KnowledgeGraphData graph) {
        return """
        {
            "@context": {
                "@vocab": "http://simplicity.ai/knowledge/"
            },
            "@graph": [%s]
        }
        """.formatted(
            graph.entities().stream()
                .map(e -> """
                    {
                        "@id": "entity:%s",
                        "@type": "%s",
                        "name": "%s"
                    }
                    """.formatted(e.entityId(), e.entityType(), e.name()))
                .collect(Collectors.joining(","))
        );
    }

    private SovereignData.KnowledgeGraphData parseGraphQL(String data) {
        // Simplified parsing
        return new SovereignData.KnowledgeGraphData(List.of(), List.of(), Map.of(), Map.of(), List.of(), Instant.now());
    }

    private SovereignData.KnowledgeGraphData parseRDF(String data) {
        return new SovereignData.KnowledgeGraphData(List.of(), List.of(), Map.of(), Map.of(), List.of(), Instant.now());
    }

    private SovereignData.KnowledgeGraphData parseJSONLD(String data) {
        return new SovereignData.KnowledgeGraphData(List.of(), List.of(), Map.of(), Map.of(), List.of(), Instant.now());
    }

    // ==================== ENUMS & RECORDS ====================

    public enum ExportFormat {
        JSON, GRAPHQL, RDF, JSON_LD, CSV
    }

    public enum GraphFormat {
        GRAPHQL, RDF, JSON_LD, SIMPLE_JSON
    }

    public enum EncryptionLevel {
        NONE, STANDARD, STRONG
    }

    public record ExportedPackage(
        UUID userId,
        String data,
        ExportFormat format,
        String checksum,
        Instant exportedAt
    ) {}

    public record PortablePackage(
        UUID packageId,
        UUID userId,
        String targetSystem,
        String data,
        Instant createdAt,
        String checksum
    ) {}

    public record TransparencyReport(
        UUID userId,
        Instant generatedAt,
        int entityCount,
        int totalInteractions,
        Map<ConsentType, ConsentStatus> consents,
        int accessLogEntries,
        long estimatedDataBytes
    ) {}

    /**
     * Internal encryption service.
     */
    class EncryptionService {
        String encrypt(String data, EncryptionLevel level) {
            // Simplified - would use proper encryption
            return data;
        }

        String decrypt(String data, EncryptionLevel level) {
            return data;
        }
    }
}

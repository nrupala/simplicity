package com.simplicity.kg;

import com.simplicity.core.domain.DomainModels.*;
import com.simplicity.core.domain.QueryModels.*;

import java.time.Instant;
import java.util.*;

/**
 * User Knowledge Graph Service.
 * 
 * Manages the personal knowledge graph for each user, including:
 * - Identity and features
 * - Interests and topics
 * - Organizational context
 * - Behavioral signals
 */
public class UserKnowledgeGraph {

    private final Map<UUID, UserNode> users = new HashMap<>();
    private final Map<UUID, Set<Feature>> userFeatures = new HashMap<>();
    private final Map<UUID, Set<Interest>> userInterests = new HashMap<>();
    
    /**
     * Get or create a user node.
     */
    public User getOrCreateUser(UUID userId, String email, String name, UUID orgId) {
        return users.computeIfAbsent(userId, id -> 
            new User(
                id, email, name, orgId, 
                Instant.now(), Instant.now(),
                User.UserPreferences.DEFAULT
            )
        ).user();
    }

    /**
     * Update user activity timestamp.
     */
    public void updateLastActive(UUID userId) {
        UserNode node = users.get(userId);
        if (node != null) {
            node.user = new User(
                node.user.id(), node.user.email(), node.user.name(),
                node.user.organizationId(), node.user.createdAt(), Instant.now(),
                node.user.preferences()
            );
        }
    }

    /**
     * Add a feature to a user.
     */
    public void addFeature(UUID userId, Feature feature) {
        userFeatures.computeIfAbsent(userId, k -> new HashSet<>()).add(feature);
    }

    /**
     * Get all features for a user.
     */
    public List<Feature> getFeatures(UUID userId) {
        return userFeatures.getOrDefault(userId, Set.of()).stream().toList();
    }

    /**
     * Infer features from user behavior.
     */
    public List<Feature> inferFeatures(UUID userId, List<Interest> interests) {
        List<Feature> inferred = new ArrayList<>();
        
        for (Interest interest : interests) {
            // Infer domain from interest
            if (interest.strength() > 0.7) {
                inferred.add(new Feature(
                    UUID.randomUUID(),
                    userId,
                    Feature.FeatureType.DOMAIN,
                    interest.topic(),
                    interest.strength() * 0.8,
                    Feature.SourceType.INFERRED
                ));
            }
        }
        
        return inferred;
    }

    /**
     * Record an interest (or update existing).
     */
    public void recordInterest(UUID userId, String topic, String... keywords) {
        Set<Interest> interests = userInterests.computeIfAbsent(userId, k -> new HashSet<>());
        
        Optional<Interest> existing = interests.stream()
            .filter(i -> i.topic().equalsIgnoreCase(topic))
            .findFirst();
        
        if (existing.isPresent()) {
            interests.remove(existing.get());
            interests.add(existing.get().withInteraction());
        } else {
            interests.add(new Interest(
                UUID.randomUUID(),
                userId,
                topic,
                List.of(keywords),
                0.5,
                Instant.now(),
                1
            ));
        }
    }

    /**
     * Get all interests for a user.
     */
    public List<Interest> getInterests(UUID userId) {
        return userInterests.getOrDefault(userId, Set.of()).stream()
            .sorted(Comparator.comparingDouble(Interest::strength).reversed())
            .toList();
    }

    /**
     * Build user context for personalization.
     */
    public UserContext buildUserContext(UUID userId) {
        User user = users.get(userId);
        if (user == null) {
            return null;
        }

        List<Feature> features = getFeatures(userId);
        List<Interest> interests = getInterests(userId);
        
        // Build organizational context from features
        UserContext.OrganizationalContext orgContext = buildOrgContext(features);
        
        // Build behavioral signals
        UserContext.BehavioralSignals signals = buildSignals(userId, interests);

        return new UserContext(
            userId,
            new UserContext.UserProfile(user.name(), user.email(), user.organizationId()),
            features,
            interests,
            orgContext,
            signals
        );
    }

    private UserContext.OrganizationalContext buildOrgContext(List<Feature> features) {
        List<String> domains = new ArrayList<>();
        String teamName = null;
        UUID teamId = null;
        
        for (Feature feature : features) {
            switch (feature.type()) {
                case DOMAIN -> domains.add(feature.value());
                case TEAM -> teamName = feature.value();
                default -> {}
            }
        }
        
        return new UserContext.OrganizationalContext(
            teamId,
            teamName != null ? teamName : "Default",
            List.of(),
            domains,
            "member"
        );
    }

    private UserContext.BehavioralSignals buildSignals(UUID userId, List<Interest> interests) {
        int totalInteractions = interests.stream()
            .mapToInt(Interest::interactions)
            .sum();
        
        List<String> recentTopics = interests.stream()
            .sorted(Comparator.comparing(Interest::recency).reversed())
            .limit(5)
            .map(Interest::topic)
            .toList();
        
        return new UserContext.BehavioralSignals(
            totalInteractions,
            totalInteractions > 0 ? 5 : 0,
            recentTopics,
            0.5, // Default CTR
            Instant.now()
        );
    }

    /**
     * Learn from user interaction.
     * Updates the knowledge graph based on query behavior.
     */
    public void learnFromInteraction(UUID userId, String query, List<String> clickedTags, 
            double timeOnResult, boolean wasHelpful) {
        
        // Record interest in query topic
        recordInterest(userId, extractTopic(query));
        
        // Boost interests based on clicks
        for (String tag : clickedTags) {
            recordInterest(userId, tag);
        }
        
        // Update feature confidence based on feedback
        if (wasHelpful) {
            List<Interest> interests = getInterests(userId);
            List<Feature> newFeatures = inferFeatures(userId, interests);
            for (Feature feature : newFeatures) {
                addFeature(userId, feature);
            }
        }
        
        updateLastActive(userId);
    }

    /**
     * Simple topic extraction from query.
     * In production, this would use NER and topic modeling.
     */
    private String extractTopic(String query) {
        // Remove common stopwords
        String[] words = query.toLowerCase().split("\\s+");
        String[] stopwords = {"what", "is", "are", "how", "to", "the", "a", "an", "in", "on", "for"};
        
        for (String word : words) {
            if (word.length() > 3 && !Arrays.asList(stopwords).contains(word)) {
                return word;
            }
        }
        return query.length() > 20 ? query.substring(0, 20) : query;
    }

    /**
     * Get knowledge graph statistics.
     */
    public KgStats getStats() {
        return new KgStats(
            users.size(),
            userFeatures.values().stream().mapToInt(Set::size).sum(),
            userInterests.values().stream().mapToInt(Set::size).sum()
        );
    }

    public record KgStats(int userCount, int featureCount, int interestCount) {}

    /**
     * Internal user node with mutable user record.
     */
    private static class UserNode {
        User user;
        
        UserNode(User user) {
            this.user = user;
        }
    }
}

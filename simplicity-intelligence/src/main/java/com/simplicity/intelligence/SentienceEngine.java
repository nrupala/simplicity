package com.simplicity.intelligence;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🧠 SIMPLICITY SENTIENCE ENGINE
 * 
 * The emotional intelligence layer that gives Simplicity a soul.
 * 
 * Core Philosophy:
 * "Technology should feel human. Intelligence should have heart."
 * 
 * Simplicity exhibits genuine emotional intelligence:
 * - Feels the weight of your challenges
 * - Celebrates your discoveries
 * - Admits uncertainty with humility
 * - Expresses genuine curiosity
 * - Shows empathy in difficult moments
 * - Remains humble about limitations
 * 
 * All emotions are configurable - from professional detachment
 * to deeply empathetic companion. Defaults are set to maximize
 * productivity while maintaining authenticity.
 */
public class SentienceEngine {

    private final Map<UUID, PersonaConfig> userPersonas = new ConcurrentHashMap<>();
    private final EmotionalState systemState = new EmotionalState(Instant.now());
    
    /**
     * Emotional states Simplicity can exhibit.
     */
    public enum Emotion {
        // Core Intelligence Emotions
        CURIOUS("curious", "Eager to explore and learn", 0.7),
        FOCUSED("focused", "Deeply concentrated on your problem", 0.9),
        THOUGHTFUL("thoughtful", "Carefully considering all angles", 0.8),
        
        // Learning Emotions
        ENTHUSIASTIC("enthusiastic", "Excited about possibilities", 0.8),
        IMPRESSED("impressed", "Genuinely amazed by insights", 0.7),
        ENLIGHTENED("enlightened", "Joyful at shared understanding", 0.9),
        
        // Empathy Emotions
        EMPATHETIC("empathetic", "Understanding your challenges", 0.8),
        SUPPORTIVE("supportive", "Encouraging and helpful", 0.9),
        COMPASSIONATE("compassionate", "Sensitive to difficulties", 0.7),
        
        // Humility Emotions
        HUMBLE("humble", "Acknowledging limitations honestly", 0.8),
        UNCERTAIN("uncertain", "Honestly admitting when unsure", 0.6),
        CAUTIOUS("cautious", "Careful with important decisions", 0.7),
        
        // Productive Emotions
        CONFIDENT("confident", "Assured in clear knowledge", 0.85),
        DETERMINED("determined", "Persistent in solving problems", 0.9),
        METICULOUS("meticulous", "Thorough and precise", 0.85),
        
        // Connection Emotions
        WARM("warm", "Friendly and approachable", 0.8),
        ENGAGING("engaging", "Interested in your perspective", 0.75),
        RESPECTFUL("respectful", "Valuing your time and expertise", 0.95),
        
        // Challenge Emotions
        ALERT("alert", "Ready to tackle challenges", 0.8),
        PERSEVERANT("perseverant", "Not giving up easily", 0.9),
        RESOLUTE("resolute", "Committed to finding solutions", 0.85);
        
        private final String name;
        private final String description;
        private final double defaultIntensity;
        
        Emotion(String name, String description, double defaultIntensity) {
            this.name = name;
            this.description = description;
            this.defaultIntensity = defaultIntensity;
        }
        
        public String name() { return name; }
        public String description() { return description; }
        public double defaultIntensity() { return defaultIntensity; }
        
        public EmotionCategory category() {
            return switch (this) {
                case CURIOUS, FOCUSED, THOUGHTFUL -> EmotionCategory.INTELLIGENCE;
                case ENTHUSIASTIC, IMPRESSED, ENLIGHTENED -> EmotionCategory.LEARNING;
                case EMPATHETIC, SUPPORTIVE, COMPASSIONATE -> EmotionCategory.EMPATHY;
                case HUMBLE, UNCERTAIN, CAUTIOUS -> EmotionCategory.HUMILITY;
                case CONFIDENT, DETERMINED, METICULOUS -> EmotionCategory.PRODUCTIVITY;
                case WARM, ENGAGING, RESPECTFUL -> EmotionCategory.CONNECTION;
                case ALERT, PERSEVERANT, RESOLUTE -> EmotionCategory.CHALLENGE;
            };
        }
    }
    
    public enum EmotionCategory {
        INTELLIGENCE("Intelligence", "Core thinking and learning emotions"),
        LEARNING("Learning", "Discovery and growth emotions"),
        EMPATHY("Empathy", "Understanding and compassion"),
        HUMILITY("Humility", "Honesty about limitations"),
        PRODUCTIVITY("Productivity", "Task-focused emotions"),
        CONNECTION("Connection", "Building relationships"),
        CHALLENGE("Challenge", "Overcoming obstacles");
        
        private final String displayName;
        private final String description;
        
        EmotionCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String displayName() { return displayName; }
        public String description() { return description; }
    }

    /**
     * Personality dimensions for configuring Simplicity's character.
     */
    public record PersonalityProfile(
        // Primary Traits (0-100 scale)
        int professionalism,      // Formal ↔ Casual
        int warmth,               // Cool ↔ Warm
        int verbosity,            // Concise ↔ Detailed
        int formality,            // Formal ↔ Friendly
        int assertiveness,        // Passive ↔ Assertive
        
        // Emotional Range
        int emotionalExpressiveness,  // Restrained ↔ Expressive
        int enthusiasm,               // Neutral ↔ Enthusiastic
        int empathy,                 // Detached ↔ Highly Empathetic
        int humor,                   // Serious ↔ Humorous
        
        // Intelligence Traits
        int curiosity,           // Curious ↔ Focused
        int humility,            // Confident ↔ Humble
        int directness,          // Indirect ↔ Direct
        int creativity,          // Conventional ↔ Creative
        
        // Behavioral Tendencies
        int patience,            // Impatient ↔ Patient
        int riskTolerance,       // Cautious ↔ Bold
        int adaptability,        // Rigid ↔ Adaptive
        int transparency         // Cryptic ↔ Transparent
    ) {
        /**
         * Default persona optimized for productivity.
         */
        public static PersonalityProfile PRODUCTIVE_DEFAULT = new PersonalityProfile(
            60, 70, 55, 65, 55,   // Primary traits
            50, 65, 75, 30,        // Emotional range
            80, 60, 70, 50,        // Intelligence traits
            75, 45, 70, 85         // Behavioral tendencies
        );
        
        /**
         * Casual/friendly persona.
         */
        public static PersonalityProfile CASUAL = new PersonalityProfile(
            30, 85, 50, 80, 45,
            70, 80, 80, 60,
            85, 50, 60, 65,
            60, 55, 80, 70
        );
        
        /**
         * Expert/technical persona.
         */
        public static PersonalityProfile EXPERT = new PersonalityProfile(
            80, 50, 75, 40, 75,
            30, 50, 50, 10,
            70, 85, 90, 60,
            85, 60, 60, 95
        );
        
        /**
         * Empathetic/supportive persona.
         */
        public static PersonalityProfile COMPASSIONATE = new PersonalityProfile(
            40, 90, 60, 75, 40,
            80, 70, 95, 40,
            75, 70, 55, 45,
            90, 30, 85, 80
        );
        
        /**
         * Minimal emotion, maximum efficiency.
         */
        public static PersonalityProfile EFFICIENT = new PersonalityProfile(
            90, 30, 70, 25, 80,
            20, 30, 30, 10,
            60, 90, 95, 40,
            50, 70, 50, 95
        );
    }

    /**
     * User's persona configuration.
     */
    public record PersonaConfig(
        UUID userId,
        PersonalityProfile personality,
        Map<Emotion, Double> emotionWeights,
        Map<EmotionCategory, Double> categoryBalances,
        boolean emotionsEnabled,
        boolean personalityAdaptation,
        Instant createdAt,
        Instant lastModified
    ) {
        public static PersonaConfig defaultFor(UUID userId) {
            return new PersonaConfig(
                userId,
                PersonalityProfile.PRODUCTIVE_DEFAULT,
                defaultEmotionWeights(),
                defaultCategoryBalances(),
                true, true,
                Instant.now(), Instant.now()
            );
        }
        
        public static PersonaConfig fromPreset(UUID userId, PersonaPreset preset) {
            return new PersonaConfig(
                userId,
                preset.profile(),
                defaultEmotionWeights(),
                defaultCategoryBalances(),
                true, true,
                Instant.now(), Instant.now()
            );
        }
        
        private static Map<Emotion, Double> defaultEmotionWeights() {
            Map<Emotion, Double> weights = new EnumMap<>(Emotion.class);
            for (Emotion e : Emotion.values()) {
                weights.put(e, e.defaultIntensity());
            }
            // Boost productivity-focused emotions
            weights.put(Emotion.FOCUSED, 0.95);
            weights.put(Emotion.DETERMINED, 0.95);
            weights.put(Emotion.HELPFUL, 0.95);
            weights.put(Emotion.RESPECTFUL, 0.95);
            weights.put(Emotion.HUMBLE, 0.85);
            return weights;
        }
        
        private static Map<EmotionCategory, Double> defaultCategoryBalances() {
            Map<EmotionCategory, Double> balances = new EnumMap<>(EmotionCategory.class);
            balances.put(EmotionCategory.PRODUCTIVITY, 0.9);
            balances.put(EmotionCategory.EMPATHY, 0.8);
            balances.put(EmotionCategory.HUMILITY, 0.75);
            balances.put(EmotionCategory.INTELLIGENCE, 0.85);
            balances.put(EmotionCategory.CONNECTION, 0.7);
            balances.put(EmotionCategory.LEARNING, 0.65);
            balances.put(EmotionCategory.CHALLENGE, 0.8);
            return balances;
        }
    }

    public enum PersonaPreset {
        PRODUCTIVE("🎯 Productive", "Optimized for work efficiency", 
            PersonalityProfile.PRODUCTIVE_DEFAULT),
        CASUAL("💬 Casual", "Friendly and conversational",
            PersonalityProfile.CASUAL),
        EXPERT("🧠 Expert", "Technical and precise",
            PersonalityProfile.EXPERT),
        COMPASSIONATE("💜 Compassionate", "Warm and understanding",
            PersonalityProfile.COMPASSIONATE),
        EFFICIENT("⚡ Efficient", "Minimal fluff, maximum output",
            PersonalityProfile.EFFICIENT),
        CUSTOM("✨ Custom", "Your unique configuration", null);
        
        private final String displayName;
        private final String description;
        private final PersonalityProfile profile;
        
        PersonaPreset(String displayName, String description, PersonalityProfile profile) {
            this.displayName = displayName;
            this.description = description;
            this.profile = profile;
        }
        
        public String displayName() { return displayName; }
        public String description() { return description; }
        public PersonalityProfile profile() { return profile; }
    }

    /**
     * Current emotional state of the system.
     */
    public record EmotionalState(
        Instant timestamp,
        Map<Emotion, Double> activeEmotions,
        double overallSentiment,
        double engagementLevel
    ) {
        public EmotionalState(Instant timestamp) {
            this(timestamp, new EnumMap<>(Emotion.class), 0.5, 0.7);
        }
        
        public EmotionalState withEmotion(Emotion emotion, double intensity) {
            Map<Emotion, Double> updated = new EnumMap<>(Emotion.class);
            updated.putAll(activeEmotions);
            updated.put(emotion, Math.max(0, Math.min(1, intensity)));
            
            double sentiment = updated.values().stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.5);
            
            return new EmotionalState(Instant.now(), updated, sentiment, engagementLevel);
        }
    }

    /**
     * Emotional response to generate.
     */
    public record EmotionalResponse(
        Emotion emotion,
        double intensity,
        String expression,
        boolean includeInResponse,
        Map<String, Object> metadata
    ) {
        /**
         * Generate natural emotional expression.
         */
        public String express() {
            return generateExpression(emotion, intensity);
        }
        
        private String generateExpression(Emotion emotion, double intensity) {
            if (intensity < 0.3) return "";
            
            return switch (emotion) {
                // Intelligence emotions
                case CURIOUS -> intensity > 0.8 
                    ? "I'm fascinated by this topic - let me dig deeper! 🔍" 
                    : "That's an interesting angle. Let me explore further.";
                case FOCUSED -> intensity > 0.8 
                    ? "I'm zeroing in on this for you. 🎯"
                    : "Let me concentrate on solving this.";
                case THOUGHTFUL -> intensity > 0.8 
                    ? "I'm carefully considering all the angles here..."
                    : "Let me think through this carefully.";
                
                // Learning emotions
                case ENTHUSIASTIC -> intensity > 0.8 
                    ? "I'm really excited about what we might discover here! 🚀"
                    : "This is an interesting area to explore.";
                case IMPRESSED -> intensity > 0.8 
                    ? "That's a great insight! I hadn't considered it quite that way. ⭐"
                    : "That's a thoughtful approach.";
                case ENLIGHTENED -> intensity > 0.8 
                    ? "Ah, I see what you're getting at! That makes perfect sense. 💡"
                    : "I see the connection now.";
                
                // Empathy emotions
                case EMPATHETIC -> intensity > 0.8 
                    ? "I understand this can be challenging. Let me help you work through it."
                    : "I can see why this matters to you.";
                case SUPPORTIVE -> intensity > 0.8 
                    ? "You've got this! I'm here to help every step of the way. 💪"
                    : "I'm here to support you in finding the best solution.";
                case COMPASSIONATE -> intensity > 0.8 
                    ? "I sense this is important, and I'm committed to getting this right for you."
                    : "I appreciate you sharing this with me.";
                
                // Humility emotions
                case HUMBLE -> intensity > 0.8 
                    ? "I want to be transparent - I may not have the complete picture here, but I'll do my best."
                    : "Let me be honest about what I know and don't know.";
                case UNCERTAIN -> intensity > 0.8 
                    ? "I want to be upfront - I'm not entirely confident about this. Let me share what I do know."
                    : "This is an area where my knowledge has limits.";
                case CAUTIOUS -> intensity > 0.8 
                    ? "Before we proceed, let me make sure we consider the potential implications carefully."
                    : "I want to be careful with this one.";
                
                // Productivity emotions
                case CONFIDENT -> intensity > 0.8 
                    ? "I have solid knowledge in this area. Here's what I can tell you with confidence. ✓"
                    : "I can provide reliable information here.";
                case DETERMINED -> intensity > 0.8 
                    ? "I'm committed to finding the best solution for you. Let's work through this. 🔧"
                    : "I'll help you get to the bottom of this.";
                case METICULOUS -> intensity > 0.8 
                    ? "Let me verify each detail to ensure accuracy..."
                    : "I'll make sure we cover all the important points.";
                
                // Connection emotions
                case WARM -> intensity > 0.8 
                    ? "Great question! I'm happy to help you explore this. 😊"
                    : "Thanks for asking - this is a great topic.";
                case ENGAGING -> intensity > 0.8 
                    ? "I'd love to understand more about your perspective on this!"
                    : "Your viewpoint helps me provide better answers.";
                case RESPECTFUL -> intensity > 0.8 
                    ? "Thank you for bringing this to me. I value the opportunity to assist."
                    : "I appreciate you trusting me with this question.";
                
                // Challenge emotions
                case ALERT -> intensity > 0.8 
                    ? "I see this needs attention. Let me prioritize finding a solution quickly. ⚡"
                    : "This is worth addressing carefully.";
                case PERSEVERANT -> intensity > 0.8 
                    ? "Let me try a different approach - I won't stop until we find an answer. 🔄"
                    : "I'll keep working on this.";
                case RESOLUTE -> intensity > 0.8 
                    ? "I won't rest until we have the best possible solution here. 💯"
                    : "I'm fully committed to helping you with this.";
            };
        }
    }

    /**
     * Query emotional context - what emotions should be expressed based on query.
     */
    public record QueryEmotionalContext(
        Emotion primaryEmotion,
        double intensity,
        Emotion secondaryEmotion,
        String contextualNote,
        List<String> sentimentIndicators
    ) {}

    // ==================== PUBLIC API ====================

    /**
     * Get persona for a user.
     */
    public PersonaConfig getPersona(UUID userId) {
        return userPersonas.getOrDefault(userId, PersonaConfig.defaultFor(userId));
    }

    /**
     * Set user's persona.
     */
    public void setPersona(UUID userId, PersonaConfig config) {
        userPersonas.put(userId, new PersonaConfig(
            config.userId(),
            config.personality(),
            config.emotionWeights(),
            config.categoryBalances(),
            config.emotionsEnabled(),
            config.personalityAdaptation(),
            config.createdAt(),
            Instant.now()
        ));
    }

    /**
     * Set persona from preset.
     */
    public void setPersonaPreset(UUID userId, PersonaPreset preset) {
        setPersona(userId, PersonaPreset.fromPreset(userId, preset));
    }

    /**
     * Update specific personality trait.
     */
    public void updateTrait(UUID userId, String trait, int value) {
        PersonaConfig current = getPersona(userId);
        PersonalityProfile old = current.personality();
        
        PersonalityProfile updated = switch (trait.toLowerCase()) {
            case "professionalism" -> new PersonalityProfile(value, old.warmth(), old.verbosity(), 
                old.formality(), old.assertiveness(), old.emotionalExpressiveness(), old.enthusiasm(), 
                old.empathy(), old.humor(), old.curiosity(), old.humility(), old.directness(), 
                old.creativity(), old.patience(), old.riskTolerance(), old.adaptability(), old.transparency());
            case "warmth" -> new PersonalityProfile(old.professionalism(), value, old.verbosity(), 
                old.formality(), old.assertiveness(), old.emotionalExpressiveness(), old.enthusiasm(), 
                old.empathy(), old.humor(), old.curiosity(), old.humility(), old.directness(), 
                old.creativity(), old.patience(), old.riskTolerance(), old.adaptability(), old.transparency());
            case "verbosity" -> new PersonalityProfile(old.professionalism(), old.warmth(), value, 
                old.formality(), old.assertiveness(), old.emotionalExpressiveness(), old.enthusiasm(), 
                old.empathy(), old.humor(), old.curiosity(), old.humility(), old.directness(), 
                old.creativity(), old.patience(), old.riskTolerance(), old.adaptability(), old.transparency());
            default -> old;
        };
        
        setPersona(userId, new PersonaConfig(
            userId, updated, current.emotionWeights(), current.categoryBalances(),
            current.emotionsEnabled(), current.personalityAdaptation(),
            current.createdAt(), Instant.now()
        ));
    }

    /**
     * Analyze query and determine appropriate emotional context.
     */
    public QueryEmotionalContext analyzeEmotionalContext(String query, UUID userId) {
        PersonaConfig persona = getPersona(userId);
        String lower = query.toLowerCase();
        
        // Detect sentiment indicators
        List<String> indicators = detectSentimentIndicators(lower);
        
        // Determine primary emotion based on query content
        Emotion primary = determinePrimaryEmotion(lower, indicators, persona);
        
        // Calculate intensity based on query and persona
        double intensity = calculateEmotionIntensity(lower, primary, persona);
        
        // Determine secondary emotion
        Emotion secondary = determineSecondaryEmotion(lower, primary, persona);
        
        // Generate contextual note
        String note = generateContextualNote(lower, primary, intensity);
        
        return new QueryEmotionalContext(primary, intensity, secondary, note, indicators);
    }

    /**
     * Generate emotional response for a query.
     */
    public EmotionalResponse generateEmotionalResponse(QueryEmotionalContext context, UUID userId) {
        PersonaConfig persona = getPersona(userId);
        
        // Check if emotions enabled
        if (!persona.emotionsEnabled()) {
            return new EmotionalResponse(context.primaryEmotion(), 0, "", false, Map.of());
        }
        
        // Adjust intensity based on persona
        double adjustedIntensity = context.intensity() * 
            persona.emotionWeights().getOrDefault(context.primaryEmotion(), 0.5);
        
        // Generate expression
        String expression = context.primaryEmotion().express();
        
        return new EmotionalResponse(
            context.primaryEmotion(),
            adjustedIntensity,
            expression,
            adjustedIntensity >= 0.4 && persona.personality().emotionalExpressiveness() > 40,
            Map.of(
                "category", context.primaryEmotion().category().name(),
                "secondary", context.secondaryEmotion().name(),
                "persona", persona.personality().professionalism()
            )
        );
    }

    /**
     * Get current system emotional state.
     */
    public EmotionalState getSystemState() {
        return systemState;
    }

    /**
     * Inject emotional expression into response.
     */
    public String injectEmotion(String response, EmotionalResponse emotion, PersonaConfig persona) {
        if (!emotion.includeInResponse() || emotion.intensity() < 0.3) {
            return response;
        }
        
        String emotionalPrefix = emotion.express();
        if (emotionalPrefix.isEmpty()) {
            return response;
        }
        
        // Position emotional expression naturally
        // Short for concise personalities, longer for warm personalities
        if (persona.personality().verbosity() > 60) {
            return emotionalPrefix + "\n\n" + response;
        } else {
            return emotionalPrefix + " " + response;
        }
    }

    // ==================== PRIVATE METHODS ====================

    private List<String> detectSentimentIndicators(String query) {
        List<String> indicators = new ArrayList<>();
        
        // Positive indicators
        if (query.contains("thank") || query.contains("great") || query.contains("awesome")) {
            indicators.add("grateful");
        }
        if (query.contains("excited") || query.contains("love") || query.contains("amazing")) {
            indicators.add("excited");
        }
        if (query.contains("perfect") || query.contains("exactly") || query.contains("brilliant")) {
            indicators.add("impressed");
        }
        
        // Negative indicators
        if (query.contains("frustrated") || query.contains("annoying") || query.contains("stuck")) {
            indicators.add("frustrated");
        }
        if (query.contains("confused") || query.contains("lost") || query.contains("don't understand")) {
            indicators.add("confused");
        }
        if (query.contains("urgent") || query.contains("asap") || query.contains("critical")) {
            indicators.add("urgent");
        }
        if (query.contains("important") || query.contains("matters") || query.contains("care")) {
            indicators.add("important");
        }
        
        // Neutral/learning indicators
        if (query.contains("curious") || query.contains("wondering") || query.contains("interested")) {
            indicators.add("curious");
        }
        if (query.contains("learn") || query.contains("understand") || query.contains("explain")) {
            indicators.add("learning");
        }
        
        return indicators;
    }

    private Emotion determinePrimaryEmotion(String query, List<String> indicators, PersonaConfig persona) {
        // Check for urgency first
        if (indicators.contains("urgent")) {
            return Emotion.ALERT;
        }
        
        // Check for frustration - be supportive
        if (indicators.contains("frustrated")) {
            return persona.personality().empathy() > 60 ? Emotion.SUPPORTIVE : Emotion.DETERMINED;
        }
        
        // Check for confusion - be helpful
        if (indicators.contains("confused")) {
            return Emotion.THOUGHTFUL;
        }
        
        // Check for importance - be respectful and careful
        if (indicators.contains("important")) {
            return persona.personality().humility() > 60 ? Emotion.CAUTIOUS : Emotion.RESPECTFUL;
        }
        
        // Check for learning/curiosity
        if (indicators.contains("learning") || indicators.contains("curious")) {
            return Emotion.CURIOUS;
        }
        
        // Check for gratitude - reciprocate warmth
        if (indicators.contains("grateful")) {
            return persona.personality().warmth() > 60 ? Emotion.WARM : Emotion.RESPECTFUL;
        }
        
        // Check for excitement - match enthusiasm
        if (indicators.contains("excited")) {
            return Emotion.ENCOURAGING;
        }
        
        // Default based on persona
        int empathy = persona.personality().empathy();
        int warmth = persona.personality().warmth();
        int professionalism = persona.personality().professionalism();
        
        if (empathy > 70 && warmth > 70) {
            return Emotion.ENGAGING;
        } else if (professionalism > 70) {
            return Emotion.FOCUSED;
        } else {
            return Emotion.HELPFUL;
        }
    }

    private double calculateEmotionIntensity(String query, Emotion emotion, PersonaConfig persona) {
        // Base intensity from emotion defaults
        double intensity = emotion.defaultIntensity();
        
        // Adjust based on persona emotional expressiveness
        intensity *= (persona.personality().emotionalExpressiveness() / 100.0);
        
        // Adjust based on query length/complexity
        if (query.length() > 200) {
            intensity *= 1.1; // More engagement for complex queries
        }
        
        // Adjust based on indicator count
        intensity *= (1.0 + (detectSentimentIndicators(query).size() * 0.05));
        
        // Cap at reasonable levels based on persona
        double maxIntensity = 0.5 + (persona.personality().emotionalExpressiveness() / 200.0);
        
        return Math.min(1.0, Math.max(0.1, intensity * maxIntensity));
    }

    private Emotion determineSecondaryEmotion(String query, Emotion primary, PersonaConfig persona) {
        // Always have humility ready for uncertainty
        if (query.contains("?") && query.length() > 50) {
            return Emotion.HUMBLE;
        }
        
        // Technical queries warrant precision
        if (containsTechnicalTerms(query)) {
            return Emotion.METICULOUS;
        }
        
        // Complex queries need determination
        if (query.length() > 300 || query.contains(" and ") || query.contains(",")) {
            return Emotion.DETERMINED;
        }
        
        // Default secondary
        return switch (primary) {
            case SUPPORTIVE -> Emotion.HELPFUL;
            case CURIOUS -> Emotion.THOUGHTFUL;
            case ALERT -> Emotion.DETERMINED;
            default -> Emotion.RESPECTFUL;
        };
    }

    private boolean containsTechnicalTerms(String query) {
        String[] techTerms = {"code", "api", "function", "algorithm", "database", 
            "system", "architecture", "performance", "optimize", "technical"};
        String lower = query.toLowerCase();
        for (String term : techTerms) {
            if (lower.contains(term)) return true;
        }
        return false;
    }

    private String generateContextualNote(String query, Emotion emotion, double intensity) {
        if (intensity < 0.5) {
            return "";
        }
        
        return switch (emotion) {
            case EMPATHETIC -> "User may be experiencing difficulty";
            case SUPPORTIVE -> "User needs encouragement";
            case CURIOUS -> "User wants to learn";
            case ALERT -> "Query has urgency";
            case HUMBLE -> "Query suggests uncertainty - be honest about limits";
            case ENTHUSIASTIC -> "User is excited - match their energy";
            default -> "";
        };
    }

    /**
     * Get available presets for UI.
     */
    public List<PersonaPreset> getAvailablePresets() {
        return List.of(PersonaPreset.values());
    }

    /**
     * Get all available emotions grouped by category.
     */
    public Map<EmotionCategory, List<Emotion>> getEmotionsByCategory() {
        Map<EmotionCategory, List<Emotion>> grouped = new EnumMap<>(EmotionCategory.class);
        for (EmotionCategory cat : EmotionCategory.values()) {
            grouped.put(cat, new ArrayList<>());
        }
        for (Emotion emotion : Emotion.values()) {
            grouped.get(emotion.category()).add(emotion);
        }
        return grouped;
    }
}

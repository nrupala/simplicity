// Personification profiles
const PROFILES = {
    curious: {
        tone: 'inquisitive',
        style: 'ask follow-up questions',
        traits: ['curious', 'engaging', 'thoughtful']
    },
    empathetic: {
        tone: 'understanding',
        style: 'show empathy and support',
        traits: ['empathetic', 'supportive', 'warm']
    },
    professional: {
        tone: 'formal',
        style: 'clear and structured',
        traits: ['professional', 'precise', 'reliable']
    },
    creative: {
        tone: 'imaginative',
        style: 'add creative insights',
        traits: ['creative', 'innovative', 'inspiring']
    }
};

// Personify response based on user preferences
async function personifyResponse(rawResponse, userId) {
    // In a real implementation, get user preferences from database
    const userProfile = await getUserProfile(userId);
    const profile = PROFILES[userProfile?.preferredTone || 'professional'];

    // Apply personification
    let personified = rawResponse;

    // Add personality markers
    if (profile.tone === 'curious') {
        personified += '\n\nWhat are your thoughts on this? I\'d love to explore this further with you.';
    } else if (profile.tone === 'empathetic') {
        personified = `I understand you're looking for information on this topic. ${personified}\n\nI hope this helps clarify things for you.`;
    } else if (profile.tone === 'creative') {
        personified += '\n\nThis reminds me of some interesting connections...';
    }

    // Adjust pacing and depth based on profile
    personified = adjustPacing(personified, profile);

    return {
        text: personified,
        profile: profile,
        traits: profile.traits
    };
}

// Get user profile (simplified)
async function getUserProfile(userId) {
    // In a real implementation, query database
    // For now, return default
    return {
        preferredTone: 'professional',
        interactionStyle: 'balanced'
    };
}

// Adjust response pacing and depth
function adjustPacing(response, profile) {
    if (profile.style.includes('structured')) {
        // Add structure
        const paragraphs = response.split('\n\n');
        if (paragraphs.length > 1) {
            return paragraphs.map((p, i) =>
                i === 0 ? p : `• ${p}`
            ).join('\n\n');
        }
    }

    return response;
}

// Generate emotional intelligence markers
function generateEmotionalMarkers(profile) {
    const markers = {
        curious: ['🤔', 'Interesting!', 'Tell me more...'],
        empathetic: ['💙', 'I understand', 'That makes sense'],
        professional: ['✅', 'According to the data', 'Based on analysis'],
        creative: ['✨', 'Imagine if...', 'What if we...']
    };

    return markers[profile.tone] || [];
}

module.exports = {
    personifyResponse,
    PROFILES
};
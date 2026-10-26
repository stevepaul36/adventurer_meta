package com.example.ui

import com.example.viewmodel.SupportedLang

/**
 * In-app language switch for the app chrome (tabs, home, shortcuts). The rider picks a language
 * in the Feed or Profile and these strings follow immediately, without changing the phone locale.
 * Screens not yet listed here stay in English.
 */
enum class Str(val en: String, val hi: String, val mr: String, val kn: String, val ta: String) {
    NAV_FEED("Feed", "फ़ीड", "फीड", "ಫೀಡ್", "ஃபீட்"),
    NAV_PASSES("Passes", "दर्रे", "खिंडी", "ಘಾಟ್‌ಗಳು", "கணவாய்கள்"),
    NAV_RIDE("Ride", "राइड", "राइड", "ರೈಡ್", "ரைடு"),
    NAV_SOS("SOS & Help", "SOS मदद", "SOS मदत", "SOS ಸಹಾಯ", "SOS உதவி"),
    NAV_RIDER("Rider", "राइडर", "रायडर", "ರೈಡರ್", "ரைடர்"),
    TAGLINE("Ride · share · stay safe", "राइड · शेयर · सुरक्षित रहें", "राइड · शेअर · सुरक्षित राहा", "ರೈಡ್ · ಹಂಚಿಕೊಳ್ಳಿ · ಸುರಕ್ಷಿತವಾಗಿರಿ", "ரைடு · பகிர் · பாதுகாப்பாக இரு"),
    HERO_TITLE("Start a ride", "राइड शुरू करें", "राइड सुरू करा", "ರೈಡ್ ಪ್ರಾರಂಭಿಸಿ", "ரைடைத் தொடங்குங்கள்"),
    HERO_BODY(
        "Press and hold to record speed, lean angle and your route.",
        "स्पीड, लीन एंगल और रूट रिकॉर्ड करने के लिए दबाकर रखें।",
        "स्पीड, लीन अँगल आणि मार्ग रेकॉर्ड करण्यासाठी दाबून ठेवा.",
        "ವೇಗ, ಲೀನ್ ಕೋನ ಮತ್ತು ಮಾರ್ಗವನ್ನು ದಾಖಲಿಸಲು ಒತ್ತಿ ಹಿಡಿಯಿರಿ.",
        "வேகம், சாய்வு கோணம் மற்றும் பாதையைப் பதிவு செய்ய அழுத்திப் பிடிக்கவும்."
    ),
    RIDE_ACTIVE("Ride in progress", "राइड जारी है", "राइड सुरू आहे", "ರೈಡ್ ನಡೆಯುತ್ತಿದೆ", "ரைடு நடந்துகொண்டிருக்கிறது"),
    OPEN_HUD("Open HUD", "HUD खोलें", "HUD उघडा", "HUD ತೆರೆಯಿರಿ", "HUD திற"),
    EXPLORE("Explore", "एक्सप्लोर करें", "एक्सप्लोर करा", "ಅನ್ವೇಷಿಸಿ", "ஆராயுங்கள்"),
    QUICK_STUDIO("AI Story Studio", "AI स्टोरी स्टूडियो", "AI स्टोरी स्टुडिओ", "AI ಸ್ಟೋರಿ ಸ್ಟುಡಿಯೋ", "AI ஸ்டோரி ஸ்டுடியோ"),
    QUICK_CLUBS("Riding clubs", "राइडिंग क्लब", "रायडिंग क्लब", "ರೈಡಿಂಗ್ ಕ್ಲಬ್‌ಗಳು", "ரைடிங் கிளப்புகள்"),
    QUICK_TROPHIES("Trophies", "ट्रॉफ़ी", "ट्रॉफी", "ಟ್ರೋಫಿಗಳು", "கோப்பைகள்"),
    QUICK_PRO("Adventurer Pro", "एडवेंचरर प्रो", "अ‍ॅडव्हेंचरर प्रो", "ಅಡ್ವೆಂಚರರ್ ಪ್ರೊ", "அட்வென்சரர் ப்ரோ"),
    POST("Post", "पोस्ट", "पोस्ट", "ಪೋಸ್ಟ್", "பதிவிடு"),
    FEED_EMPTY(
        "No posts yet — finish a ride or tap Post to share one.",
        "अभी कोई पोस्ट नहीं — राइड पूरी करें या पोस्ट पर टैप करें।",
        "अजून पोस्ट नाहीत — राइड पूर्ण करा किंवा पोस्टवर टॅप करा.",
        "ಇನ್ನೂ ಪೋಸ್ಟ್‌ಗಳಿಲ್ಲ — ರೈಡ್ ಮುಗಿಸಿ ಅಥವಾ ಪೋಸ್ಟ್ ಒತ್ತಿ.",
        "இன்னும் பதிவுகள் இல்லை — ஒரு ரைடை முடிக்கவும் அல்லது பதிவிடு என்பதைத் தட்டவும்."
    );

    fun get(lang: SupportedLang): String = when (lang) {
        SupportedLang.ENGLISH -> en
        SupportedLang.HINDI -> hi
        SupportedLang.MARATHI -> mr
        SupportedLang.KANNADA -> kn
        SupportedLang.TAMIL -> ta
    }
}

package com.example.model

enum class AspectRatioOption(val label: String, val shortLabel: String, val ratio: Float?) {
    ORIGINAL("Original (Source)", "Original", null),
    RATIO_16_9("16:9 Landscape (YouTube)", "16:9", 16f / 9f),
    RATIO_9_16("9:16 Vertical (Reel / TikTok / Shorts)", "9:16", 9f / 16f),
    RATIO_1_1("1:1 Square (Instagram Post)", "1:1", 1f),
    RATIO_4_5("4:5 Portrait (Social Feed)", "4:5", 4f / 5f),
    RATIO_21_9("21:9 Cinematic (Ultra-Wide)", "21:9", 21f / 9f)
}

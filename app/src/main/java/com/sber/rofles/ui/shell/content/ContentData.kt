package com.sber.rofles.ui.shell.content

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.sber.rofles.R

internal enum class ContentKind(val label: String, @DrawableRes val icon: Int) {
    Video("видео", R.drawable.content_ic_play),
    Course("курс", R.drawable.content_ic_play),
    Test("тест", R.drawable.content_ic_calc),
}

internal data class ContentItem(
    @DrawableRes val image: Int,
    val kind: ContentKind,
    val title: String,
    val likes: String = "999 тыс",
    val progress: Float? = null,
)

internal data class TopBannerItem(
    @DrawableRes val image: Int,
    val title: String,
)

internal data class StoryItem(
    @DrawableRes val image: Int,
    @DrawableRes val tag: Int,
    val background: Color? = null,
    val viewed: Boolean = false,
)

internal data class ContentCategory(val title: String, val iconColor: Color?)

internal val topBanners = listOf(
    TopBannerItem(R.drawable.content_banner_cat, "Котик и\nсмартфон"),
    TopBannerItem(R.drawable.content_banner_luana, "Агент Kids Луана"),
    TopBannerItem(R.drawable.content_banner_cat, "Котик и\nсмартфон"),
    TopBannerItem(R.drawable.content_banner_luana, "Агент Kids Луана"),
)

internal val stories = listOf(
    StoryItem(R.drawable.content_story_quiz, R.drawable.content_ic_story_quiz, background = Color(0xFF5893FF)),
    StoryItem(R.drawable.content_story_2, R.drawable.content_ic_story_play),
    StoryItem(R.drawable.content_story_4, R.drawable.content_ic_story_bulb),
    StoryItem(R.drawable.content_story_3, R.drawable.content_ic_story_play),
    StoryItem(R.drawable.content_story_2, R.drawable.content_ic_story_play, viewed = true),
)

internal val categories = listOf(
    ContentCategory("Все", null),
    ContentCategory("ИИ", Color(0xFF4DB0FF)),
    ContentCategory("Деньги", Color(0xFF19D13B)),
    ContentCategory("Скиллы", Color(0xFFFF8A1F)),
    ContentCategory("Творчество", Color(0xFFFF5297)),
    ContentCategory("Наука", Color(0xFF8B5CF6)),
)

private const val TWO_LINES = "Короткое название в две строки"

internal val feedTop = listOf(
    ContentItem(R.drawable.content_card_money, ContentKind.Video, "Как зарабатывать и тратить деньги по-взрослому", progress = 0.75f),
    ContentItem(R.drawable.content_card_jar, ContentKind.Course, "Накопи на мечту", progress = 0.75f),
)

internal val top5 = listOf(
    ContentItem(R.drawable.content_top_1, ContentKind.Video, TWO_LINES),
    ContentItem(R.drawable.content_top_2, ContentKind.Video, TWO_LINES),
    ContentItem(R.drawable.content_top_1, ContentKind.Video, TWO_LINES),
    ContentItem(R.drawable.content_top_2, ContentKind.Video, TWO_LINES),
    ContentItem(R.drawable.content_top_1, ContentKind.Video, TWO_LINES),
)

internal val feedMiddle = listOf(
    ContentItem(R.drawable.content_card_robot, ContentKind.Test, "Короткое название в_одну строку"),
)

internal val algorithmics = listOf(
    ContentItem(R.drawable.content_top_1, ContentKind.Video, TWO_LINES, progress = 0.6f),
    ContentItem(R.drawable.content_top_2, ContentKind.Video, TWO_LINES),
    ContentItem(R.drawable.content_top_1, ContentKind.Video, TWO_LINES),
)

internal val feedBottom = listOf(
    ContentItem(R.drawable.content_card_girl, ContentKind.Video, "Короткое название в_одну строку"),
    ContentItem(R.drawable.content_card_money, ContentKind.Video, "Короткое название в_одну или даже в_две строки максимум"),
)

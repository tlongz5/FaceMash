package com.example.facemash.utils

import com.example.facemash.R
import com.example.facemash.model.LanguageModel

object Constants {
    const val LANGUAGE_KEY = "LANGUAGE_KEY"
    const val LANGUAGE_CODE = "LANGUAGE_CODE"
    const val USE_SONG_DEFAULT = "USE_SONG_DEFAULT"

    const val MATH = "Math"
    const val MUSIC = "Music"

    const val URL_TERM_AND_POLICY = "https://gianghastudio.online/policy"

    object ActionSendMail {
        const val RECIPIENT_EMAIL = "thangnd99308@gmail.com"
        const val MAIL_TO = "mailto:"
        const val INTENT_TITLE = "Choose an Email client :"
    }

    const val RATE_ENABLE = "RATE_ENABLE"
    const val GAVE_GOOD_RATED = "GAVE_GOOD_RATED"

    object FilterType {

        const val ALL_FILTER = "All Filter" // get all data
        const val FACE_PUZZLE = "face puzzle"
        const val FUNNY_FACE = "funny face"
        const val ZOOM_PUZZLE = "zoom puzzle"
        const val LIP_POSES = "lip poses"
        const val SPIN_PUZZLE = "spin puzzle"
        const val RANKING = "Ranking"
        const val GOAT = "goat"
        const val LOGO = "logo"
        const val MATH = "math"
        const val SONG = "song"
        const val TEXT = "Text"
        const val TEXT_MIX = "Text Mix"
    }

    const val GAME_RECORD_FOLDER = "FaceMash"

    val filterType = listOf(
        FilterType.ALL_FILTER,
        FilterType.FACE_PUZZLE,
        FilterType.FUNNY_FACE,
        FilterType.ZOOM_PUZZLE,
        FilterType.LIP_POSES,
        FilterType.SPIN_PUZZLE,
        FilterType.RANKING,
        FilterType.GOAT,
        FilterType.LOGO,
        FilterType.MATH,
        FilterType.SONG
    )

    val rankingType = listOf(
        "Social",
        "Beauty",
        "Food",
        "Female",
        "Eye",
        "Nail",
        "Bug",
        "Polite Hug",
        "Brand",
        "Kiss",
        "Lips",
        "Snacks",
        "Dog",
        "Housing",
        "Car",
        "Hand Bag",
        "Football",
        "Action",
        "Woman"
    )

    val rankingTypeMultiList = List(10){rankingType}.flatten()


}
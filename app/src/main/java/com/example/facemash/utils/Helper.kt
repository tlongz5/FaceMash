package com.example.facemash.utils

import com.example.facemash.R
import com.example.facemash.model.IntroPager
import com.example.facemash.model.LanguageModel

object Helper {
    val introList = listOf(
        IntroPager(
            name = R.string.intro_title_1,
            des = R.string.intro_des_1,
            image = R.drawable.intro_1
        ),
        IntroPager(
            name = R.string.intro_title_2,
            des = R.string.intro_des_2,
            image = R.drawable.intro_2
        ),
        IntroPager(
            name = R.string.intro_title_3,
            des = R.string.intro_des_3,
            image = R.drawable.intro_3
        )
    )

    val languageList = mutableListOf(
        LanguageModel(
            image = R.drawable.ic_language_english,
            name = "English",
            "en",
            isSelected = false,
            imgSelected = R.drawable.bg_language_english
        ),
        LanguageModel(
            image = R.drawable.ic_language_russia,
            name = "Russian",
            "ru",
            isSelected = false,
            imgSelected = R.drawable.bg_language_russia
        ),
        LanguageModel(
            image = R.drawable.ic_language__japan,
            name = "Japanese",
            "ja",
            isSelected = false,
            imgSelected = R.drawable.bg_language_japanese
        ),
        LanguageModel(
            image = R.drawable.ic_language_india,
            name = "हिंदी",
            "hi",
            isSelected = false,
            imgSelected = R.drawable.bg_language_india
        ),
        LanguageModel(
            image = R.drawable.ic_language_korea,
            name = "한국인",
            "ko",
            isSelected = false,
            imgSelected = R.drawable.bg_language_korea
        ),
        LanguageModel(
            image = R.drawable.ic_language_espana,
            name = "España",
            "es",
            isSelected = false,
            imgSelected = R.drawable.bg_language_espana
        ),
        LanguageModel(
            image = R.drawable.ic_language_portugues,
            name = "Português",
            "pt",
            isSelected = false,
            imgSelected = R.drawable.bg_language_portugues
        ),
        LanguageModel(
            image = R.drawable.ic_language_indonesia,
            name = "Indonesia",
            "in",
            isSelected = false,
            imgSelected = R.drawable.bg_language_indonesia
        ),
        LanguageModel(
            image = R.drawable.ic_language_de,
            name = "Deutsch",
            "de",
            isSelected = false,
            imgSelected = R.drawable.bg_language_de
        ),
        LanguageModel(
            image = R.drawable.ic_language_zh,
            name = "中国人",
            "zh",
            isSelected = false,
            imgSelected = R.drawable.bg_language_zh
        ),
        LanguageModel(
            image = R.drawable.ic_language_thailand,
            name = "ThaiLand",
            "th",
            isSelected = false,
            imgSelected = R.drawable.bg_language_thailand
        ),
        LanguageModel(
            image = R.drawable.ic_language_vietnamese,
            name = "Tiếng Việt",
            "vi",
            isSelected = false,
            imgSelected = R.drawable.bg_language_vietnamese
        ),
        LanguageModel(
            image = R.drawable.ic_language_france,
            name = "Français",
            "fr",
            isSelected = false,
            imgSelected = R.drawable.bg_language_france
        ),
        LanguageModel(
            image = R.drawable.ic_language_italia,
            name = "Italiano",
            "it",
            isSelected = false,
            imgSelected = R.drawable.bg_language_italia
        )
    )
}

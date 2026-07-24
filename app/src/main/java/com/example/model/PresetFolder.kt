package com.example.model

import com.example.R

data class PresetFolder(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val pictures: List<PictureItem>
)

object PresetFolderData {
    val animeFolder = PresetFolder(
        id = "anime_art",
        name = "Anime & Artwork",
        description = "Sample folder containing anime character illustrations",
        iconEmoji = "🎨",
        pictures = listOf(
            PictureItem(
                id = "pic_1",
                fileName = "kittew.png",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_kittew_1784899213518}",
                drawableResId = R.drawable.img_kittew_1784899213518
            ),
            PictureItem(
                id = "pic_2",
                fileName = "magion02.png",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_magion02_1784899231320}",
                drawableResId = R.drawable.img_magion02_1784899231320
            ),
            PictureItem(
                id = "pic_3",
                fileName = "natsuishi nana.png",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_natsuishi_nana_1784899245200}",
                drawableResId = R.drawable.img_natsuishi_nana_1784899245200
            ),
            PictureItem(
                id = "pic_4",
                fileName = "miku_vocaloid_stage.png",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_magion02_1784899231320}",
                drawableResId = R.drawable.img_magion02_1784899231320
            ),
            PictureItem(
                id = "pic_5",
                fileName = "sakura_dreamland_v2.jpg",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_natsuishi_nana_1784899245200}",
                drawableResId = R.drawable.img_natsuishi_nana_1784899245200
            ),
            PictureItem(
                id = "pic_6",
                fileName = "cyberpunk_city_chibi.png",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_kittew_1784899213518}",
                drawableResId = R.drawable.img_kittew_1784899213518
            ),
            PictureItem(
                id = "pic_7",
                fileName = "starlight_reverie.webp",
                uriString = "android.resource://com.aistudio.picpicker.rnd789/${R.drawable.img_magion02_1784899231320}",
                drawableResId = R.drawable.img_magion02_1784899231320
            )
        )
    )

    val natureFolder = PresetFolder(
        id = "nature_landscapes",
        name = "Nature & Wallpapers",
        description = "Scenic landscape photos and high-res wallpapers",
        iconEmoji = "🌄",
        pictures = listOf(
            PictureItem(id = "nat_1", fileName = "mountain_peak_sunset.jpg", uriString = ""),
            PictureItem(id = "nat_2", fileName = "misty_pine_forest.png", uriString = ""),
            PictureItem(id = "nat_3", fileName = "tropical_beach_caribbean.png", uriString = ""),
            PictureItem(id = "nat_4", fileName = "autumn_maple_leaves.webp", uriString = ""),
            PictureItem(id = "nat_5", fileName = "cosmic_starlight_nebula.png", uriString = ""),
            PictureItem(id = "nat_6", fileName = "emerald_waterfall.jpg", uriString = ""),
            PictureItem(id = "nat_7", fileName = "snowy_alps_panorama.png", uriString = "")
        )
    )

    val petsFolder = PresetFolder(
        id = "cute_pets",
        name = "Pets & Animals",
        description = "Adorable cat, dog, and wildlife photography",
        iconEmoji = "🐾",
        pictures = listOf(
            PictureItem(id = "pet_1", fileName = "fluffy_kitten_sleep.png", uriString = ""),
            PictureItem(id = "pet_2", fileName = "golden_retriever_smile.jpg", uriString = ""),
            PictureItem(id = "pet_3", fileName = "shiba_inu_park.png", uriString = ""),
            PictureItem(id = "pet_4", fileName = "hamster_sunflower_seed.png", uriString = ""),
            PictureItem(id = "pet_5", fileName = "capybara_spa_day.webp", uriString = ""),
            PictureItem(id = "pet_6", fileName = "curious_penguin.jpg", uriString = "")
        )
    )

    val allPresets = listOf(animeFolder, natureFolder, petsFolder)
}

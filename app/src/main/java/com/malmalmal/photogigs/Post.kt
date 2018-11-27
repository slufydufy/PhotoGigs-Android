package com.malmalmal.photogigs


class Post (val name : String, val pd : String, val imageUrl : String, val caption : String, val userImageUrl : String) {
    constructor() : this("", "", "", "", "")
}
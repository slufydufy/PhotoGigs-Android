package com.malmalmal.photogigs


class Post (val postId : String, val pd : String, val imageUrl : String, val caption : String, val uuid : String, val order : String) {
    constructor() : this("", "", "", "", "", "")
}
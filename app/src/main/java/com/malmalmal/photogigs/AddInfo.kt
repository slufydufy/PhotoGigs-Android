package com.malmalmal.photogigs

class AddInfo (val postId : String, val kamera : String, val lensa : String, val flash : String, val diafragma : String, val iso : String, val speed : String) {
    constructor() : this("", "", "", "", "", "", "")
}
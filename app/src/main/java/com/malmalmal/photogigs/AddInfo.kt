package com.malmalmal.photogigs

class AddInfo (val postId : String, val kamera : String, val lensa : String, val lokasi : String) {
    constructor() : this("", "", "", "")
}
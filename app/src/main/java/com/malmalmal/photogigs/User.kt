package com.malmalmal.photogigs

class User(val uuid : String, val name : String, val userImageUrl : String, val about : String) {
    constructor() : this("", "", "", "")
}
package com.malmalmal.photogigs

class Mission (val imgPP : String, val img : String, val mistitle : String, val brief : String, val sdate : String, val edate : String, val org : String, val desc : String, val prize : String, val pdate : String, val id : Long) {
    constructor() : this("","","","","","","","","","",0)
}
package com.malmalmal.photogigs

class Event (val etitle : String, val infos : String, val contents : String, val edate : String, val sdate : String, val img : String, val pdate : String) {
    constructor() : this("", "", "", "", "", "", "")
}
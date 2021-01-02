package com.alperenbabagil.taggy

import java.util.*
import kotlin.random.Random

class Suggester {

    fun suggest(text:String) : String{
        //return text + UUID.randomUUID().toString().substring(0,Random.nextInt(1,5))
        return text + ('a'..'z').map { it }.shuffled().subList(0, Random.nextInt(1,5)).joinToString("")
    }
}
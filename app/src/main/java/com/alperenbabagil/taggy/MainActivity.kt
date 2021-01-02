package com.alperenbabagil.taggy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var currentSearchJob : Job?=null
        val suggester = Suggester()
        taggyView.apply {
            suggestedTagsLimit=6
            selectedTagsLimit=4
            setSelectedTags(listOf("happy","style","life"))
            setSuggestedTags(listOf("photo","nature","cute","insta","model","music","travel","likesforlike"))
            textEnteredCallback = {text->
                currentSearchJob?.cancel()
                setLoadingState(true)
                currentSearchJob=lifecycleScope.launch{
                    delay(Random.nextLong(1000,2500))
                    launch (Dispatchers.Main){
                        setSuggestedTags(sequence {
                            repeat(Random.nextInt(1,10)){
                                yield(suggester.suggest(text))
                            }
                        }.toList())
                        setLoadingState(false)
                    }
                }

                if(text=="premium"){
                    showWarningMessage("Click here to be premium",5000){ id, message ->
                        Toast.makeText(this@MainActivity,
                            "You are premium",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
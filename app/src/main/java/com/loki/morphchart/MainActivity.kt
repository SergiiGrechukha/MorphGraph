package com.loki.morphchart

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private var sectorOneUp = false
    private var sectorTwoUp = false
    private var sectorThreeUp = false
    private var sectorFourUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val graph = findViewById<MorphChart>(R.id.graph)

        findViewById<Button>(R.id.click_me).setOnClickListener({
            graph.animate(1000)
        })


        findViewById<Button>(R.id.sector_one).setOnClickListener({
            sectorOneUp = !sectorOneUp
            graph.morphSector(0,4, morphRatio = 1.5f, morphUp = sectorOneUp)
        })

        findViewById<Button>(R.id.sector_two).setOnClickListener({
            sectorTwoUp = !sectorTwoUp
            graph.morphSector(1,4, morphRatio = 1.5f, morphUp = sectorTwoUp)
        })

        findViewById<Button>(R.id.sector_three).setOnClickListener({
            sectorThreeUp = !sectorThreeUp
            graph.morphSector(2,4, morphRatio = 1.5f, morphUp = sectorThreeUp)
        })

        findViewById<Button>(R.id.sector_four).setOnClickListener({
            sectorFourUp = !sectorFourUp
            graph.morphSector(3,4, morphRatio = 1.5f, morphUp = sectorFourUp)
        })
    }
}

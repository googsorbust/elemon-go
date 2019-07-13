package org.tensorflow.lite.examples.detection

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView

class PeriodicTableActivity : AppCompatActivity() {

    val ROWS = 18

    val elements = listOf(
        Element("H", 1, "1", "Hydrogen", false),
        Element("He", 2, "2", "Helium", false),
        Element("asdsad", 3, "3", "asfsfDadsgegrdfd", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_periodic_table)


        val adapter = PeriodicTableAdapter(this, elements)


        val table = findViewById<RecyclerView>(R.id.periodic_table_recycler)
        val tableLayoutManager = GridLayoutManager(this, ROWS)
        table.layoutManager = tableLayoutManager
        table.adapter = adapter
    }


}

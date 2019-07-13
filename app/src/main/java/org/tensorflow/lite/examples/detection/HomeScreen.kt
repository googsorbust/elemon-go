package org.tensorflow.lite.examples.detection

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

import java.text.MessageFormat

class HomeScreen : AppCompatActivity() {

    private var points: Int = 0
    private val pointKey = "POINTS"
    private val pointTemplate = "You have {0} points!"
    private var sharedPref: SharedPreferences? = null

    private val pointsFromSharedPreferences: Int
        get() = sharedPref!!.getInt(pointKey, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        sharedPref = this@HomeScreen.getPreferences(Context.MODE_PRIVATE)
        init()
    }

    fun showDetectionScreen(view: View) {
        val myIntent = Intent(this@HomeScreen, DetectorActivity::class.java)
        this@HomeScreen.startActivity(myIntent)
    }

    fun showMyElementsScreen(view: View) {
//        val myIntent = Intent(this@HomeScreen, MyElements::class.java)
//        this@HomeScreen.startActivity(myIntent)

    }

    private fun init() {
        updatePointsOnView()
    }

    private fun writePointsToSharedPreferences(newPoints: Int) {
        val editor = sharedPref!!.edit()
        editor.putInt(pointKey, newPoints)
        editor.apply()
    }

    private fun updatePointsOnView() {
        val result = pointsFromSharedPreferences
        points = if (result >= 0) result else 0

        val params = arrayOf<Any>(points)
        val formattedPoints = MessageFormat.format(pointTemplate, *params)
        val view = findViewById<TextView>(R.id.pointLabel)
        view.text = formattedPoints
    }

    private fun addPoints(pointsToAdd: Int) {
        points += pointsToAdd
        writePointsToSharedPreferences(points)
        updatePointsOnView()
    }
}

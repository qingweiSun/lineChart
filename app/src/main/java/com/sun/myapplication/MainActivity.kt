package com.sun.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lineChart1.subjectColor = "07C377"

        lineChart2.setXAxis(mutableListOf<String>().apply {
            add("02-26")
            add("02-27")
            add("02-28")
            add("02-29")
            add("03-01")
            add("03-02")
            add("03-03")
            add("03-04")
        })
        lineChart2.setYAxis(mutableListOf<String>().apply {
            add("0")
            add("1")
            add("2")
            add("3")
            add("4")
            add("5")
        })

        lineChart2.yAxisData = LinkedList<Float>().apply {
            add(1f)
            add(3f)
            add(2f)
            add(4.4f)
            add(4f)
            add(1f)
            add(5f)
            add(3.5f)
        }
        lineChart2.subjectColor = "FAD437"
        lineChart2.max = 5f


        lineChart3.yAxisData = LinkedList<Float>().apply {
            add(1f)
            add(300f)
            add(20f)
            add(400.4f)
            add(467f)
            add(146f)
            add(50f)
            add(369.5f)
        }
        lineChart3.subjectColor = "3F8FFF"
        lineChart4.yAxisData = LinkedList<Float>().apply {
            add(100f)
            add(20f)
            add(200f)
            add(40.4f)
            add(47f)
            add(16f)
            add(500f)
            add(369.5f)
        }
    }
}
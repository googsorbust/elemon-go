package org.tensorflow.lite.examples.detection

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PeriodicTableAdapter(
    val context: Context,
    val elements: List<Element>
) : RecyclerView.Adapter<ElementViewHolder>() {
    override fun getItemCount(): Int = elements.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ElementViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.table_item, parent, false)
        val holder = ElementViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ElementViewHolder, i: Int) {
        holder.init(elements[i])
    }
}

class ElementViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val elementNumber: TextView = view.findViewById(R.id.element_number_found)
    private val elementName: TextView = view.findViewById(R.id.element_name_found)
    private val elementSymbol: TextView = view.findViewById(R.id.element_symbol_found)
    private val elementWeight: TextView = view.findViewById(R.id.element_weight_found)


    fun init(element: Element) {
        elementNumber.text = element.number.toString()
        elementName.text = element.name
        elementSymbol.text = element.symbol
        elementWeight.text = element.weight
        view.setBackgroundColor(Color.argb(255, 255, 255 / element.number, 255))
    }

}

data class Element(
    val symbol: String,
    val number: Int,
    val weight: String,
    val name: String,
    val discovered: Boolean
)
package org.tensorflow.lite.examples.detection

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class RelativeSquareLayout : RelativeLayout {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onMeasure(widthMeasureSpec: Int, ignored: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}
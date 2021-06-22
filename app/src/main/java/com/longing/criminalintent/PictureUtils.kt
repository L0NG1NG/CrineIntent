package com.longing.criminalintent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, context: Context): Bitmap {
    val size = Point()
    context.display?.getRealSize(size)

    return getScaledBitmap(path, size.x, size.y)

}

fun getScaledBitmap(path: String, destWith: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    //用于读取位图尺寸
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWith = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()
    //Figure out how much to scale by
    var inSampleSize = 1
    if (srcWith > destWith || srcHeight > destHeight) {

        val heightScale = srcHeight / destHeight
        val widthScale = srcWith / destWith
        //强啊 扩展函数
        val sampleScale = heightScale.coerceAtLeast(widthScale)
        inSampleSize = sampleScale.roundToInt()

    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    //创建最终的位图
    return BitmapFactory.decodeFile(path, options)

}

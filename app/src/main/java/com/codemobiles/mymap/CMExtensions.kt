package com.codemobiles.mymap

import android.content.Context
import android.widget.Toast


fun Context.showToast(message: String){
    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
}

fun String.Codemobiles(message: String): String{
    return message + "5555555"
}



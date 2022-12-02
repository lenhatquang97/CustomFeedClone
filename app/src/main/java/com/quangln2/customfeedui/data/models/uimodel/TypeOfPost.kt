package com.quangln2.customfeedui.data.models.uimodel

enum class TypeOfPost(val value: Int) {
    ADD_NEW_POST(0),
    POST(1),
    HEADER(2),
    BODY(3),
    FOOTER(4)
}

fun getTypeOfPost(value: Int): TypeOfPost{
    return when(value){
        0 -> TypeOfPost.ADD_NEW_POST
        1 -> TypeOfPost.POST
        2 -> TypeOfPost.HEADER
        3 -> TypeOfPost.BODY
        else -> TypeOfPost.FOOTER
    }
}
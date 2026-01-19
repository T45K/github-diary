package io.github.t45k.githubDiary.util

fun <T> List<T>.set(index: Int, element: T): List<T> =
    mapIndexed { i, t -> if (i == index) element else t }

fun <T> List<T>.remove(index: Int): List<T> =
    filterIndexed { i, _ -> i != index } 

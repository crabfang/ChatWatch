package com.cabe.app.watch.utils

/**
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */

fun <T> isCollectionEmpty(collection: Collection<T>?) : Boolean {
    if (collection == null || collection.isEmpty()) {
        return true
    }
    return false
}

fun <T> isCollectionNotEmpty(collection: Collection<T>?) : Boolean {
    return !isCollectionEmpty(collection)
}

fun <T> collectionSize(collection: Collection<T>?): Int {
    return collection?.size ?: 0
}

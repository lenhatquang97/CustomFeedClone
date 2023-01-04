package com.quangln2.customfeedui.data.models.datamodel

import org.json.JSONArray
import org.json.JSONObject


data class OfflineResource(
    var url: String,
    var size: Long,
    var bytesCopied: Long
) {
    override fun equals(other: Any?): Boolean {
        if (other is OfflineResource) {
            val sameUrl = url == other.url
            val sameSize = size == other.size
            val sameBytesCopied = bytesCopied == other.bytesCopied
            return sameUrl && sameSize && sameBytesCopied
        }
        return false
    }
    companion object{
        fun offlineResourceToJsonObject(obj: OfflineResource): JSONObject{
            val jsonObject = JSONObject()
            jsonObject.apply {
                put("url", obj.url)
                put("size", obj.size)
                put("bytesCopied", obj.bytesCopied)
            }

            return jsonObject
        }
        fun jsonStringToOfflineResource(json: String): OfflineResource{
            val jsonObject = JSONObject(json)
            return OfflineResource(
                jsonObject.getString("url"),
                jsonObject.getLong("size"),
                jsonObject.getLong("bytesCopied")
            )
        }

        fun mutableListOfflineResourceToJsonArray(list: MutableList<OfflineResource>): String{
            val jsonArray = JSONArray()
            list.forEach {
                jsonArray.put(offlineResourceToJsonObject(it))
            }
            return jsonArray.toString()
        }

        fun jsonArrayToMutableListOfflineResource(json: String): MutableList<OfflineResource>{
            val jsonArray = JSONArray(json)
            val list = mutableListOf<OfflineResource>()
            for (i in 0 until jsonArray.length()){
                list.add(jsonStringToOfflineResource(jsonArray.getString(i)))
            }
            return list
        }
    }

}

package com.quangln2.customfeedui.data.models.others

import org.json.JSONArray
import org.json.JSONObject

data class UploadWorkerModel(
    val caption: String,
    val uriLists: List<String>
) {
    companion object{
        fun uploadWorkerToJson(obj: UploadWorkerModel): JSONObject {
            val jsonObject = JSONObject()
            val jsonResourceArray = JSONArray()
            obj.uriLists.forEach {
                jsonResourceArray.put(it)
            }
            jsonObject.put("caption", obj.caption)
            jsonObject.put("uriLists", jsonResourceArray)
            return jsonObject
        }
        fun jsonStringToUploadWorker(json: String): UploadWorkerModel{
            val jsonObject = JSONObject(json)
            val caption = jsonObject.getString("caption")
            val uriLists = mutableListOf<String>()
            val jsonResourceArray = jsonObject.getJSONArray("uriLists")
            for (i in 0 until jsonResourceArray.length()){
                uriLists.add(jsonResourceArray.getString(i))
            }
            return UploadWorkerModel(caption, uriLists)
        }
    }
}

package com.even.zining.inherit.sound.start.newfun
import com.even.zining.inherit.sound.tool.data.FnnBean
import com.even.zining.inherit.sound.start.DataGetUtils
import com.even.zining.inherit.sound.tool.data.FnnLoadData
import com.even.zining.inherit.sound.tool.data.MMKVUtils
import com.google.gson.Gson

object DataStorage {
    fun getAdminData(): FnnBean? {
//        MMKVUtils.put(FnnLoadData.admindata, FnnLoadData.json_data)
        val adminData = MMKVUtils.getString(FnnLoadData.admindata)
        return runCatching {
            Gson().fromJson(adminData, FnnBean::class.java)
        }.getOrNull()
    }

    fun putAdminData(adminBean: String) {
        MMKVUtils.put(FnnLoadData.admindata, adminBean)
//        MMKVUtils.put(FnnLoadData.admindata, FnnLoadData.json_data)
        DataGetUtils.initFaceBook()
    }
}

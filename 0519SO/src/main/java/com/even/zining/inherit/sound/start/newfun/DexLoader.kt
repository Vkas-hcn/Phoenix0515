package com.even.zining.inherit.sound.start.newfun
import android.app.Application
import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.io.FileOutputStream

object DexLoader {
    fun runAppInitFromAssets(context: Context, mustXSData: Boolean) {
        try {
            val dexFile = copyDexFromAssets(context, "classes.dex", "classes.dex")
            val dexPath = dexFile.absolutePath
            val classLoader = DexClassLoader(dexPath, context.cacheDir.path, null, context.classLoader)

            ClassReflector.invokeAppInit(classLoader, context, mustXSData)
        } catch (e: Exception) {
            Log.e("TAG", "runAppInitFromAssets failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun copyDexFromAssets(context: Context, assetFileName: String, destFileName: String): File {
        val destFile = File(context.filesDir, destFileName)
        if (destFile.exists()) return destFile

        context.assets.open(assetFileName).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }

        destFile.setReadable(true, false)
        destFile.setWritable(true, false)
        return destFile
    }
}

object ClassReflector {
    fun invokeAppInit(classLoader: DexClassLoader, context: Context, mustXSData: Boolean) {
        try {
            val helperClass = classLoader.loadClass("com.clouds.desire.appinit.AppInit")
            val field = helperClass.getDeclaredField("INSTANCE")
            val instance = field.get(null)
            val method = helperClass.getDeclaredMethod("init", Application::class.java, Boolean::class.java)
            method.invoke(instance, context.applicationContext, mustXSData)
        } catch (e: Exception) {
            Log.e("TAG", "反射调用失败: ${e.message}")
            e.printStackTrace()
        }
    }
}

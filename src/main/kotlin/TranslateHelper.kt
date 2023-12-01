import com.google.gson.Gson
import java.io.FileInputStream
import java.io.InputStreamReader

class TranslateHelper {
    companion object {
        private fun getTranslates(): Map<*, *>? {
            return Gson().fromJson<Map<*, *>>(
                InputStreamReader(FileInputStream("${DevTools.basedir}/src/main/resources/assets/primogemcraft/lang/zh_cn.json")),
                MutableMap::class.java
            )
        }

        private fun getEnglish(): Map<*, *>? {
            return Gson().fromJson<Map<*, *>>(
                InputStreamReader(FileInputStream("${DevTools.basedir}/src/main/resources/assets/primogemcraft/lang/en_us.json")),
                MutableMap::class.java
            )
        }

        fun getTranslationName(name: String): String {
            val translates = getTranslates()
            return (translates?.get("item.primogemcraft.$name")?.toString()
                ?: translates?.get("block.primogemcraft.$name"))?.toString() ?: ""
        }

        fun getEnglishName(name: String): String {
            val translates = getEnglish()
            return (translates?.get("item.primogemcraft.$name")?.toString()
                ?: translates?.get("block.primogemcraft.$name"))?.toString() ?: ""
        }
    }
}
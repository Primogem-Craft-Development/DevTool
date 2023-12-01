import TranslateHelper.Companion.getEnglishName
import TranslateHelper.Companion.getTranslationName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*

enum class TextureType(val value: String) {
    BLOCK("block"), ITEM("item");
}

class RenameHelper {
    companion object {
        fun writeLang(key: String, id: String) {
            val chs = getTranslationName(id)
            val eng = getEnglishName(id)
            val path = File("${DevTools.output}/src/main/resources/assets/primogemcraft/lang/zh_cn.json").toPath()
            val pathEn = File("${DevTools.output}/src/main/resources/assets/primogemcraft/lang/en_us.json").toPath()
            val lang = Gson().fromJson(Files.readString(path), Map::class.java)
                .map { Pair(it.key.toString(), it.value.toString()) } as ArrayList
            val langEn = Gson().fromJson(Files.readString(pathEn), Map::class.java)
                .map { Pair(it.key.toString(), it.value.toString()) } as ArrayList
            val prefix = key.substringBefore('.')
            var flag = false
            for (it in lang) {
                if (it.first.startsWith("$prefix.")) {
                    flag = true
                } else if (flag) {
                    val ind = lang.indexOf(it)
                    lang.add(ind, Pair(key, chs))
                    langEn.add(ind, Pair(key, eng))
                    break
                }
            }
            if (!flag) {
                lang.add(Pair(key, chs))
                langEn.add(Pair(key, eng))
            }
            Files.writeString(
                path,
                GsonBuilder().setPrettyPrinting().create().toJson(lang.toMap()),
                StandardOpenOption.TRUNCATE_EXISTING
            )
            Files.writeString(
                pathEn,
                GsonBuilder().setPrettyPrinting().create().toJson(langEn.toMap()),
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }

        fun writeItemModel(id: String, new: String, scanner: Scanner, mh: ModelHelper? = null) {
            val model = Gson().fromJson(
                Files.readString(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/item/$id.json").toPath()),
                Map::class.java
            ).toMutableMap()
            if (model["parent"].toString().startsWith("primogemcraft:block/")) {
                model["parent"] = "primogemcraft:block/$new"
                writeAsset("models/item/${new}.json", model)
                return
            }
            println("物品模型 -> $model")
            mh ?: ModelHelper(scanner, TextureType.ITEM).renameTextures(model)
            writeAsset("models/item/${new}.json", model)
        }

        fun writeBlockstates(id: String, new: String, scanner: Scanner) {
            val states = Gson().fromJson(
                Files.readString(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/blockstates/$id.json").toPath()),
                Map::class.java
            ).toMutableMap()
            println("方块状态 -> $states")
            val model = ModelHelper(scanner, TextureType.BLOCK)
            val map = HashMap<String, String>()
            processVariants(states, scanner, model, map)
            processMultipart(states, scanner, model, map)
            writeAsset("blockstates/$new.json", states)
            if (File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/item/$id.json").exists()) {
                model.type = TextureType.ITEM
                writeItemModel(id, new, scanner, model)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun processMultipart(
            states: MutableMap<Any?, Any?>, scanner: Scanner, mh: ModelHelper, modelMap: HashMap<String, String>
        ) {
            if (!states.contains("multipart")) return
            val multipart = states["multipart"] as List<Map<*, *>>
            for (it in multipart) {
                val apply = it["apply"] as MutableMap<String, String>
                println("条件when{${it["when"]}}模型 -> ${apply["model"]}")
            }
            for (it in multipart) {
                val apply = it["apply"] as MutableMap<String, String>
                val model = apply["model"]
                if (!modelMap.containsKey(model)) {
                    println("重命名模型 (when{${it["when"]}} $ ${model}): ")
                    print("-> ")
                    modelMap[model!!] = scanner.next()
                }
                val nm = modelMap[model]!!
                apply["model"] = "primogemcraft:block/$nm"
                writeBlockModel(model.toString().substringAfter('/'), nm, mh)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun processVariants(
            states: MutableMap<Any?, Any?>, scanner: Scanner, mh: ModelHelper, modelMap: HashMap<String, String>
        ) {
            if (!states.contains("variants")) return
            for ((key, variant) in states["variants"] as Map<*, *>) {
                if (variant is Map<*, *>) {
                    val model = variant["model"]
                    println("状态${key}模型 -> $model")
                }
            }
            for ((key, variant) in states["variants"] as Map<*, *>) {
                if (variant is Map<*, *>) {
                    val model = variant["model"]
                    if (!modelMap.containsKey(model)) {
                        print("重命名模型 (${if (key == "") "default" else key} $ ${model}) -> ")
                        modelMap[model.toString()] = scanner.next()
                    }
                    val nm = modelMap[model]!!
                    ((states["variants"] as MutableMap<*, *>)[key] as MutableMap<String, String>)["model"] =
                        "primogemcraft:block/$nm"
                    writeBlockModel(model.toString().substringAfter('/'), nm, mh)
                }
            }
        }

        private fun writeBlockModel(id: String, new: String, mh: ModelHelper) {
            val model = Gson().fromJson(
                Files.readString(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/block/$id.json").toPath()),
                Map::class.java
            ).toMutableMap()
            println("方块模型 -> $model")
            mh.renameTextures(model)
            writeAsset("models/block/${new}.json", model)
        }


        private fun writeAsset(path: String, json: MutableMap<Any?, Any?>) {
            File("${DevTools.output}/src/main/resources/assets/primogemcraft/$path").also { it.parentFile.mkdirs() }
                .also { it.createNewFile() }.apply {
                    Files.writeString(
                        this.toPath(),
                        GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(json),
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                }
        }
    }
}

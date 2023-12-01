import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

class RenameHelper {
    enum class TextureType(val value: String) {
        BLOCK("block"), ITEM("item");
    }

    companion object {
        fun writeLang(key: String, value: String) {
            val path = File("${DevTools.output}/src/main/resources/assets/primogemcraft/lang/zh_cn.json").toPath()
            val lang = Gson().fromJson(Files.readString(path), Map::class.java)
                .map { Pair(it.key.toString(), it.value.toString()) } as ArrayList
            val prefix = key.substringBefore('.')
            var flag = false
            for (it in lang) {
                if (it.first.startsWith("$prefix.")) {
                    flag = true
                } else if (flag) {
                    lang.add(lang.indexOf(it), Pair(key, value))
                    break
                }
            }
            if (!flag) lang.add(Pair(key, value))
            Files.writeString(
                path,
                GsonBuilder().setPrettyPrinting().create().toJson(lang.toMap()),
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }

        fun writeItemModel(id: String, new: String, scanner: Scanner) {
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
            renameTextures(model, scanner, TextureType.ITEM)
            writeAsset("models/item/${new}.json", model)
        }

        @Suppress("UNCHECKED_CAST")
        fun writeBlockstates(id: String, new: String, scanner: Scanner) {
            val states = Gson().fromJson(
                Files.readString(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/blockstates/$id.json").toPath()),
                Map::class.java
            ).toMutableMap()
            println("方块状态 -> $states")
            for ((key, variant) in states["variants"] as Map<*, *>) {
                if (variant is Map<*, *>) {
                    val model = variant["model"]
                    println("状态${key}模型 -> $model")
                }
            }
            for ((key, variant) in states["variants"] as Map<*, *>) {
                if (variant is Map<*, *>) {
                    val model = variant["model"]
                    print("重命名模型 (${if (key == "") "default" else key} $ ${model}) -> ")
                    val nm = scanner.next()
                    ((states["variants"] as MutableMap<*, *>)[key] as MutableMap<String, String>)["model"] =
                        "primogemcraft:block/$nm"
                    writeBlockModel(model.toString().substringAfter('/'), nm, scanner)
                }
            }
            writeAsset("blockstates/$new.json", states)
            if (File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/item/$id.json").exists()) {
                writeItemModel(id, new, scanner)
            }
        }

        private fun writeBlockModel(id: String, new: String, scanner: Scanner) {
            val model = Gson().fromJson(
                Files.readString(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/block/$id.json").toPath()),
                Map::class.java
            ).toMutableMap()
            println("方块模型 -> $model")
            renameTextures(model, scanner, TextureType.BLOCK)
            writeAsset("models/block/${new}.json", model)
        }

        private fun renameTextures(model: MutableMap<Any?, Any?>, scanner: Scanner, type: TextureType) {
            val remapped = HashMap<String, String>()
            for (t in model["textures"] as Map<*, *>) {
                print("重命名纹理 (${t.key} $ ${t.value}) -> ")
                remapped[t.key.toString()] = renameTexture(t, scanner.next(), type)
            }
            model["textures"] = remapped
        }

        private fun renameTexture(texture: Map.Entry<*, *>, original: String, type: TextureType): String {
            val file = "${DevTools.basedir}/src/main/resources/assets/primogemcraft/textures/${
                texture.value.toString().replace(
                    "primogemcraft:", ""
                )
            }.png"
            val extract =
                "${DevTools.output}/src/main/resources/assets/primogemcraft/textures/${type.value}/$original.png"
            File(extract).parentFile.mkdirs()
            try {
                Files.copy(Path.of(file), Path.of(extract))
            } catch (_: Exception) {
            }
            return "primogemcraft:${type.toString().lowercase()}/$original"
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

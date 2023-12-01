import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ModelHelper(private val scanner: Scanner, var type: TextureType) {
    private val mapping = HashMap<String, String>()

    fun renameTextures(model: MutableMap<Any?, Any?>) {
        val remapped = HashMap<String, String>()
        for (t in model["textures"] as Map<*, *>) {
            val key = t.key.toString()
            val value = t.value.toString()
            if (!mapping.containsKey(value)) {
                print("重命名纹理 ($key $ $value) -> ")
                mapping[value] = renameTexture(t, scanner.next())
            }
            remapped[key] = mapping[value]!!
        }
        model["textures"] = remapped
    }

    private fun renameTexture(texture: Map.Entry<*, *>, name: String): String {
        val file = "${DevTools.basedir}/src/main/resources/assets/primogemcraft/textures/${
            texture.value.toString().replace(
                "primogemcraft:", ""
            )
        }.png"
        val extract = "${DevTools.output}/src/main/resources/assets/primogemcraft/textures/${type.value}/$name.png"
        File(extract).parentFile.mkdirs()
        try {
            Files.copy(Path.of(file), Path.of(extract))
        } catch (_: Exception) {
        }
        return "primogemcraft:${type.toString().lowercase()}/$name"
    }
}
import TranslateHelper.Companion.getEnglishName
import TranslateHelper.Companion.getTranslationName
import java.io.File
import java.util.*
import java.util.function.Consumer

class ItemHelper {
    companion object {
        private fun listItems(): List<File> {
            return Arrays.stream(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/models/item").listFiles())
                .toList()
        }

        fun renameItem(scanner: Scanner) {
            try {
                val id = scanner.next()
                println("物品名称 -> ${getTranslationName(id)}(${getEnglishName(id)})")
                print("重命名物品 ID -> ")
                val new = scanner.next()
                RenameHelper.writeLang("item.primogemcraft.$new", id)
                RenameHelper.writeItemModel(id, new, scanner)
            } catch (e: Exception) {
                println("没有该物品或发生了错误 $e")
            }
        }

        fun findItems() {
            val result = listItems()
            result.forEach(Consumer { file: File ->
                if (file.isFile()) {
                    val name = file.getName().replace(".json", "")
                    System.out.printf("%s -> %s\n", name, getTranslationName(name))
                }
            })
            println("共 ${result.size} 个物品")
        }
    }
}
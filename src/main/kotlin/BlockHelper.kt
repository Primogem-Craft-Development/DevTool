import TranslateHelper.Companion.getTranslationName
import java.io.File
import java.util.*
import java.util.function.Consumer

class BlockHelper {
    companion object {
        private fun listBlocks(): List<File> {
            return Arrays.stream(File("${DevTools.basedir}/src/main/resources/assets/primogemcraft/blockstates").listFiles())
                .toList()
        }

        fun findBlocks() {
            val result = listBlocks()
            result.forEach(Consumer { file: File ->
                if (file.isFile()) {
                    val name = file.getName().replace(".json", "")
                    System.out.printf("%s -> %s\n", name, getTranslationName(name))
                }
            })
            println("共 ${result.size} 个方块")
        }

        fun renameBlock(scanner: Scanner) {
            try {
                val id = scanner.next()
                println("方块名称 -> ${getTranslationName(id)}")
                print("重命名方块 ID -> ")
                val new = scanner.next()
                RenameHelper.writeLang("block.primogemcraft.$new", getTranslationName(id))
                RenameHelper.writeBlockstates(id, new, scanner)
            } catch (e: Exception) {
                println("没有该方块或发生了错误 $e")
            }
        }
    }
}
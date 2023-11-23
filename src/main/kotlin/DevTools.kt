import java.io.FileInputStream
import java.util.*
import kotlin.system.exitProcess

class DevTools {
    companion object {
        @JvmField
        val basedir: String

        @JvmField
        val output: String

        init {
            val fi = FileInputStream("devtool.cfg")
            val pro = Properties()
            pro.load(fi)
            fi.close()
            basedir = pro["basedir"] as String
            output = pro["output"] as String
        }
    }
}

fun main() {
    if (DevTools.basedir == "null") {
        println("请设置basedir")
        return
    }
    if (DevTools.output == "null") {
        println("请设置output")
        return
    }
    val scanner = Scanner(System.`in`)
    while (true) {
        print("> ")
        when (val s: String? = scanner.next()) {
            "li", "lis-items" -> ItemHelper.findItems()
            "ri", "rename-item" -> ItemHelper.renameItem(scanner)
            "lb", "lis-blocks" -> BlockHelper.findBlocks()
            "rb", "rename-block" -> BlockHelper.renameBlock(scanner)
            "exit" -> exitProcess(0)
            else -> println("未找到命令 $s")
        }
    }
}
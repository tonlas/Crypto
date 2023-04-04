package cryptography


import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

import java.io.IOException
import kotlin.experimental.xor


fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        val input = readln()
        println(
            when (input) {
                "hide" -> hide()
                "show" -> show()
                "exit" -> {
                    println("Bye!")
                    return
                }

                else -> "Wrong task: $input"
            }
        )
    }
}

fun show() {
    println("Input image file:")
    val inputFile = readln()
    try {
        val messageContainer = mutableListOf<Byte>()
        ImageIO.read(File(inputFile)).run {
            var letter = ""
            lbl1@ for (i in 0 until height) {
                for (j in 0 until width) {
                    val color = Color(getRGB(j, i))
                    letter += (color.blue and 1).toString()

                    if (letter.length == 8) {
                        messageContainer.add(letter.toByte(2))
                        if (messageContainer.takeLast(3)== listOf<Byte>(0,0,3)
                            && messageContainer.size >= 3
                        ) {
                            repeat(3) { messageContainer.removeLast() }
                            break@lbl1
                        }
                        letter = ""
                    }
                }
            }
        }
        println("Password:")
        val password = readln().toByteArray()

        val message = messageContainer.toByteArray().encrypt(password)
        println("Message:")
        println(message.toString(Charsets.UTF_8))
    } catch (e: IOException) {
        println("Can't read input file!")
    }
}

fun hide() {
    println("Input image file:")
    val inputFile = readln()
    try {
        ImageIO.read(File(inputFile)).run {
            println("Output image file:")
            val outputFile = readln()

            println("Message to hide:")
            val message = readln().toByteArray()

            println("Password:")
            val password = readln().toByteArray()

            val encryptedMessage = message.encrypt(password).map { character ->
                character.toString(2).toMutableList()
                    .also {
                        while (it.size < 8) {
                            it.add(0, '0')
                        }
                    }.joinToString("")
            }.toMutableList().apply {
                add("00000000")
                add("00000000")
                add("00000011")
            }.joinToString("")

            if (encryptedMessage.length > width * height) {
                println("The input image is not large enough to hold this message.")
                return
            }
            var bitIndex = 0
            loop1@ for (i in 0 until height) {
                for (j in 0 until width) {

                    setRGB(j, i, getRGB(j,i).let{if(encryptedMessage[bitIndex]=='1') it or 1 else it and 1.inv()})
                    if (bitIndex == encryptedMessage.lastIndex) break@loop1 else bitIndex++

                }
            }
            ImageIO.write(this, "png", File(outputFile))
            println("Message saved in $outputFile image.")
        }
    } catch (e: IOException) {
        println("Can't read input file!")
    }
}

fun ByteArray.encrypt(password:ByteArray):ByteArray{
    val encryptedArray= mutableListOf<Byte>()
    for (index in this.indices) {
        encryptedArray.add(this[index] xor password[index%password.size])
    }
    return encryptedArray.toByteArray()
}



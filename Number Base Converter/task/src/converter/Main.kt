package converter

import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun main() {
    mainLoop@ while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")
        val input = readLine()!!
        val (sourceBase, targetBase) = when (input) {
            "/exit" -> return
            else -> input.split(" ").map { it.toInt() }
        }
        while (true) {
            println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
            val num = readLine()!!
            when {
                num == "/back" -> break
                num == "/exit" -> break@mainLoop
                num == "0" -> println("Conversion result: 0")
                '.' in num -> println("Conversion result: ${ convertFraction(num, sourceBase, targetBase) }")
                else -> println("Conversion result: ${ convert(num, sourceBase, targetBase) }")
            }
        }
    }
}

fun convertFraction(num: String, sourceBase: Int, targetBase: Int): String {
    val integerPart = num.substringBefore('.')
    val fractionalPart = num.substringAfter('.')
    var returnsNothing = true
    for (i in fractionalPart) {
        if (i != '0') {
            returnsNothing = false
            break
        }
    }

    val targetIntegerPart = convert(integerPart, sourceBase, targetBase)
    val decimalFractionalPart = fractionalToDecimal(fractionalPart, sourceBase)
    val targetFractionPart = when (returnsNothing) {
        false -> fromDecimalFractional(decimalFractionalPart, targetBase)
        else -> "00000"
    }
    return "$targetIntegerPart.$targetFractionPart"
}

fun fromDecimalFractional(fractionalPart: BigDecimal, targetBase: Int): String {
    var counter = fractionalPart.setScale(5, RoundingMode.DOWN)
    val alphabet = ('a'..'z').toMutableList()
    var numString = ""
    var digitCounter = 0
    while (counter != BigInteger.ZERO && digitCounter < 5) {
        counter *= BigDecimal(targetBase.toString())
        if (counter.toString().substringBefore('.').length < 2) {
            numString += counter.toString().substringBefore('.')
        } else {
            numString += alphabet[counter.toString().substringBefore('.').toInt() - 10]
        }
        counter = BigDecimal("0." + counter.toString().substringAfter('.'))
        digitCounter++
    }
    return numString
}

fun fractionalToDecimal(fractionalPart: String, sourceBase: Int): BigDecimal {
    var num = BigDecimal("0.0")
    var counter = -1
    val mathContext = MathContext(5)
    for (i in fractionalPart) {
        num += if (i.isDigit()) {
            BigDecimal(i.toString()) * sourceBase.toBigDecimal().pow(counter, mathContext)
        } else {
            BigDecimal(i.lowercaseChar().code - 87) * sourceBase.toBigDecimal().pow(counter, mathContext)
        }
        counter--
    }
    return num
}

fun convert(num: String, sourceBase: Int, targetBase: Int): String {
    val decimal = toDecimal(num, sourceBase).toBigInteger()
    return fromDecimal(decimal, targetBase)
}

fun fromDecimal(decimal: BigInteger, targetBase: Int): String {
    var counter = decimal
    val targetBaseBigInt = targetBase.toBigInteger()
    val alphabet = ('a'..'z').toMutableList()
    var numString = ""
    while (counter != BigInteger.ZERO) {
        numString += when (counter % targetBaseBigInt) {
            in BigInteger("0")..BigInteger("9") -> counter % targetBaseBigInt
            else -> alphabet[(counter % targetBaseBigInt - BigInteger("10")).toInt()]
        }
        counter /= targetBaseBigInt
    }
    return numString.reversed()
}

fun toDecimal(sourceNum: String, sourceBase: Int): String {
    var counter = BigInteger.ONE
    var num = BigInteger.ZERO
    for (i in sourceNum.reversed()) {
        num += counter * if (i.isDigit()) {
            BigInteger(i.toString())
        } else {
            BigInteger.valueOf((i.lowercaseChar().code - 87).toLong())
        }
        counter *= sourceBase.toBigInteger()
    }
    return num.toString()
}

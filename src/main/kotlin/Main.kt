import kotlin.math.pow

// Использовать метод параболы

// Точность вычислений
const val E = 1e-7

// Порядок точности метода парабол
const val K = 4

class ProcessData(
    val f: (x: Double) -> (Double),
    var a: Double,
    var b: Double,
    val exactIntegral: Double? = null,
    val c: Double? = null
)

data class Result(
    val n: Int,
    val integral: Double,
    val kDelta: Double,
    val deltaExact: Double? = null,
    val deltaRunge: Double,
    val deltaTheory: Double? = null
) {
    override fun toString(): String {
        return "n: $n\t| integral = ${String.format("%.7e", integral)}" +
                "\t| kDelta = ${String.format("%.1f", kDelta)}${
                    if (deltaExact != null)
                        "\t| deltaExact = ${String.format("%.5e", deltaExact)}"
                    else
                        ""
                }\t| deltaRunge = ${String.format("%.5e", deltaRunge)}${
                    if (deltaTheory != null)
                        "\t| deltaTheory = ${String.format("%.5e", deltaTheory)}"
                    else
                        ""
                }"
    }
}

// n - заданное разбиение
fun numIntegral(data: ProcessData, n: Int): Double {
    val h = (data.b - data.a) / n

    // Узловые точки (по x)
    val nodalPoints = arrayListOf<Double>()
    for (i in 0..n) {
        nodalPoints.add(data.a + i * h)
    }

    var sum1 = 0.0
    for (i in 1 until n) {
        sum1 += data.f(nodalPoints[i])
    }
    var sum2 = 0.0
    for (i in 0 until n) {
        sum2 += data.f(nodalPoints[i] + h / 2)
    }

    // Квадратурная формула Ньютона-Котеса
    // Метод парабол
    return (h / 6) * (data.f(data.a) + data.f(data.b) + 2 * sum1 + 4 * sum2)
}

fun process(data: ProcessData): List<Result> {
    val result = arrayListOf<Result>()
    var runge = -1.0
    var integral: Double
    var integralH2: Double
    var integralH4: Double
    var integralDiv2: Double

    var n = 2
    while (Math.abs(runge) > E) {
        integral = numIntegral(data, n)
        integralH2 = numIntegral(data, n * 2)
        integralH4 = numIntegral(data, n * 4)
        integralDiv2 = numIntegral(data, n / 2)
        runge = (integral - integralDiv2) / (2.0.pow(K) - 1)

        result.add(
            Result(
                // Разбиение отрезка [a, b] на n узлов
                n = n,

                // Значение интеграла при разбиении = n
                integral = integral,

                // Коэффициент скорость уменьшения погрешности при уменьшении h - 2.7
                kDelta = (integralH2 - integral) / (integralH4 - integralH2),

                // Точная дельта
                deltaExact = if (data.exactIntegral != null) Math.abs(data.exactIntegral - integral) else null,

                // Оценка по Рунге - 2.6
                deltaRunge = runge,

                // Теоретическая дельта - 2.3
                deltaTheory = if (data.c != null) Math.abs(data.c * ((data.b - data.a) / n).pow(K)) else null
            )
        )

        n *= 2
    }

    return result
}

fun main() {
    // Отладочный пример
    process(
        ProcessData(
            f = { 6 * it.pow(5) },
            a = 0.0,
            b = 1.0,
            exactIntegral = 1.0,
            c = 720.0 / 2880
        )
    ).let {
        it.forEach { println(it) }
        println("Значение интеграла 6*x^5 на отрезке [0, 1] = ${it.last().integral}")
        println()
    }

    process(
        ProcessData(
            f = { it.pow(1.0 / 30) * Math.sqrt(1 + it * it) },
            a = 0.0,
            b = 1.5
        )
    ).let {
        it.forEach { println(it) }
        println("Значение интеграла x^(1/30)*sqrt(1+x^2) на отрезке [0, 1.5] = ${it.last().integral}")
        println()
    }

    process(
        ProcessData(
            f = { it.pow(1.0 / 30) * Math.sqrt(1 + it * it) },
            a = 0.001,
            b = 1.5
        )
    ).let {
        it.forEach { println(it) }
        println("Значение интеграла x^(1/30)*sqrt(1+x^2) на отрезке [0.001, 1.5] = ${it.last().integral}")
    }
}
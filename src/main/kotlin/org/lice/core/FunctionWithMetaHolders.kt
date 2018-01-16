@file:Suppress("FunctionName")

package org.lice.core

import org.lice.Lice
import org.lice.lang.NumberOperator
import org.lice.model.MetaData
import org.lice.util.*
import org.lice.util.InterpretException.Factory.typeMisMatch
import java.lang.reflect.Modifier
import java.nio.file.Paths
import java.util.*

@Suppress("unused")
class FunctionWithMetaHolders(private val symbolList: SymbolList) {
	fun `-`(meta: MetaData, it: List<Any?>) = when (it.size) {
		0 -> 0
		1 -> it.first(meta)
		else -> it.drop(1).fold(NumberOperator(it.first() as Number)) { sum, value ->
			if (value is Number) sum.minus(value, meta)
			else typeMisMatch("Number", value, meta)
		}.result
	}

	fun `+`(meta: MetaData, it: List<Any?>) =
			it.fold(NumberOperator(0)) { sum, value ->
				if (value is Number) sum.plus(value, meta)
				else typeMisMatch("Number", value, meta)
			}.result

	fun extern(meta: MetaData, it: List<Any?>): Any? {
		val name = it[1, meta].toString()
		val clazz = it.first(meta).toString()
		val method = Class.forName(clazz).declaredMethods
				.firstOrNull { Modifier.isStatic(it.modifiers) && it.name == name }
				?: throw UnsatisfiedLinkError("Method $name not found for class $clazz\nat line: ${meta.beginLine}")
		symbolList.provideFunction(name) { runReflection { method.invoke(null, *it.toTypedArray()) } }
		return name
	}

	fun `==`(meta: MetaData, ls: List<Any?>) = (1 until ls.size)
			.all { NumberOperator.compare(cast(ls[it - 1]), cast(ls[it]), meta) == 0 }

	fun `!=`(meta: MetaData, ls: List<Any?>) = (1 until ls.size)
			.none { NumberOperator.compare(cast(ls[it - 1]), cast(ls[it]), meta) == 0 }

	fun `%`(meta: MetaData, ls: List<Any?>) = when (ls.size) {
		0 -> 0
		1 -> ls.first()
		else -> ls.drop(1)
				.fold(NumberOperator(cast(ls.first()))) { sum, value ->
					if (value is Number) sum.rem(value, meta)
					else typeMisMatch("Number", value, meta)
				}.result
	}

	fun `*`(meta: MetaData, ls: List<Any?>) = ls.fold(NumberOperator(1)) { sum, value ->
		if (value is Number) sum.times(value, meta)
		else typeMisMatch("Number", value, meta)
	}.result

	fun format(meta: MetaData, ls: List<Any?>) =
			String.format(ls.first(meta).toString(), *ls.subList(1, ls.size).toTypedArray())

	fun sqrt(meta: MetaData, it: List<Any?>) = Math.sqrt(cast<Number>(it.first(meta)).toDouble())
	fun sin(meta: MetaData, it: List<Any?>) = Math.sin(cast<Number>(it.first(meta)).toDouble())
	fun tan(meta: MetaData, it: List<Any?>) = Math.tan(cast<Number>(it.first(meta)).toDouble())
	fun asin(meta: MetaData, it: List<Any?>) = Math.asin(cast<Number>(it.first(meta)).toDouble())
	fun atan(meta: MetaData, it: List<Any?>) = Math.atan(cast<Number>(it.first(meta)).toDouble())
	fun sinh(meta: MetaData, it: List<Any?>) = Math.sinh(cast<Number>(it.first(meta)).toDouble())
	fun tanh(meta: MetaData, it: List<Any?>) = Math.tanh(cast<Number>(it.first(meta)).toDouble())
	fun exp(meta: MetaData, it: List<Any?>) = Math.exp(cast<Number>(it.first(meta)).toDouble())
	fun log(meta: MetaData, it: List<Any?>) = Math.log(cast<Number>(it.first(meta)).toDouble())
	fun log10(meta: MetaData, it: List<Any?>) = Math.log10(cast<Number>(it.first(meta)).toDouble())
	fun eval(meta: MetaData, it: List<Any?>) = Lice.run(it.first(meta).toString(), symbolList = symbolList)
	fun type(meta: MetaData, it: List<Any?>) = it.first(meta)?.javaClass ?: Nothing::class.java
	fun `load-file`(meta: MetaData, it: List<Any?>) = Lice.run(Paths.get(it.first(meta).toString()), symbolList)
	fun `!`(meta: MetaData, it: List<Any?>) = it.first(meta).booleanValue().not()
	fun `~`(meta: MetaData, it: List<Any?>) = cast<Int>(it.first(meta)).inv()
	fun `!!`(meta: MetaData, it: List<Any?>): Any? {
		val a = it.first(meta)
		return when (a) {
			is Iterable<*> -> a.toList()[cast(it[1])]
			is Array<*> -> a[cast(it[1])]
			else -> null
		}
	}

	private val liceScanner = Scanner(System.`in`)
	fun getInts(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta) ?: 1)).map { liceScanner.nextInt() }
	fun getFloats(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.nextFloat() }
	fun getDoubles(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.nextDouble() }
	fun getLines(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.nextLine() }
	fun getTokens(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.next() }
	fun getBigInts(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.nextBigInteger() }
	fun getBigDecs(meta: MetaData, it: List<Any?>) = (1..cast(it.first(meta))).map { liceScanner.nextBigDecimal() }
	fun `in?`(meta: MetaData, it: List<Any?>) = it[1, meta] in cast<Iterable<*>>(it.first(meta))
	fun size(meta: MetaData, it: List<Any?>) = cast<Iterable<*>>(it.first(meta)).count()
	fun last(meta: MetaData, it: List<Any?>) = cast<Iterable<*>>(it.first(meta)).last()
	fun reverse(meta: MetaData, it: List<Any?>) = cast<Iterable<*>>(it.first(meta)).reversed()
	fun chunk(meta: MetaData, it: List<Any?>) = cast<Iterable<*>>(it.first(meta)).chunked(cast(it[1, meta]))
	fun `++`(meta: MetaData, it: List<Any?>) = cast<Iterable<*>>(it.first(meta)) + cast<Iterable<*>>(it[1, meta])
	fun sort(meta: MetaData, it: List<Any?>) = cast<Iterable<Comparable<Comparable<*>>>>(it.first(meta)).sorted()
	fun split(meta: MetaData, it: List<Any?>) = it.first(meta).toString().split(it[1].toString()).toList()
	fun count(meta: MetaData, it: List<Any?>) =
			it[1, meta].let { e -> cast<Iterable<*>>(it.first(meta)).count { it == e } }

	fun `&`(meta: MetaData, it: List<Any?>) =
			it.map { cast<Number>(it, meta).toInt() }.reduce { last, self -> last and self }

	fun `|`(meta: MetaData, it: List<Any?>) =
			it.map { cast<Number>(it, meta).toInt() }.reduce { last, self -> last or self }

	fun `^`(meta: MetaData, it: List<Any?>) =
			it.map { cast<Number>(it, meta).toInt() }.reduce { last, self -> last xor self }

}
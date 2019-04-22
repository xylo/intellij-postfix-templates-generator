package de.endrullis.idea.postfixtemplatesgenerator

import java.io.{File, PrintStream}
import java.lang.reflect.Modifier

import org.apache.commons.beanutils.ConstructorUtils
import org.apache.commons.io.{EndianUtils, FileUtils, FilenameUtils, IOUtils}
import org.apache.commons.lang3._
import resource._

import scala.io.Source

/**
 * Template file generator for the Intellij IDEA plugin "Custom Postfix Templates".
 *
 * @author Stefan Endrullis
 */
object PostfixTemplateGenerator {

	val templateDir = new File("templates")

	val utilsCollections = List(
		UtilsCollection("commons-lang", "Apache commons-lang3", classOf[ArrayUtils], classOf[BooleanUtils], classOf[CharSetUtils],
			classOf[CharUtils], classOf[ClassUtils], classOf[LocaleUtils], classOf[ObjectUtils], classOf[RegExUtils],
			classOf[SerializationUtils], classOf[StringUtils], classOf[SystemUtils], classOf[ThreadUtils]),
		
		UtilsCollection("commons-io", "Apache commons-io", classOf[EndianUtils], classOf[FilenameUtils], classOf[FileUtils], classOf[IOUtils]),

		//UtilsCollection("commons-codec", "Apache commons-codec", classOf[])
		// Hex decode/encode

		UtilsCollection("commons-beanutils", "Apache commons-beanutils", classOf[ConstructorUtils]),
	)
	val langs = List(
		Lang("java", "ARRAY", _.getCanonicalName),
		Lang("scala", "scala.Array", _.getCanonicalName.replaceFirst(".*\\.", ""))
	)

	def main(args: Array[String]) {
		for (lang ← langs; utilsCollection ← utilsCollections) {
			val dir = new File(templateDir, lang.name)
			generateTemplateFile(dir, utilsCollection, lang)
		}
	}

	def generateTemplateFile(dir: File, utilsCollection: UtilsCollection, lang: Lang) {
		dir.mkdirs()
		
		val file = new File(dir, utilsCollection.name + ".postfixTemplates")
		val out = new PrintStream(file)
		out.println(s"## Templates for ${utilsCollection.description} ##")

		for (utilClass ← utilsCollection.utilClasses) {
			out.println()
			out.println("## " + utilClass.getName.replaceFirst(".*\\.", ""))
			out.println()
			printTemplates(utilClass, lang, out)
		}

		out.close()
	}

	def printTemplates(utilClass: Class[_], lang: Lang, out: PrintStream) {
		val allMethods = utilClass.getMethods.toList.filter{m ⇒
			Modifier.isStatic(m.getModifiers) &&
			Modifier.isPublic(m.getModifiers) &&
			m.getParameterCount > 0 &&
			m.getAnnotation(classOf[Deprecated]) == null &&
			!lang.classMethodExcludes.contains(lang.mapType(m.getParameterTypes.head), m.getName)
		}

		allMethods.groupBy(_.getName).filterNot(_._1.contains("_")).foreach{case (name, methods) ⇒
			out.println("." + name + " : " + name)

			methods.groupBy(m ⇒ lang.mapType(m.getParameterTypes.head)).foreach{case (matchingType, ms) ⇒
				val params = ms.filter(_.getParameterCount > 1).map(_.getParameterTypes()(1)).toSet

				val className = utilClass.getCanonicalName
				val utilClassName = lang.utilClassName(utilClass)

				val leftSide = s"$matchingType [$className]"

				val rightSide = if (params.isEmpty) {
					s"$utilClassName.$name($$expr$$)"
				} else {
					s"$utilClassName.$name($$expr$$, $$arg$$)"
				}

				out.println(s"\t$leftSide  →  $rightSide")
			}
			out.println()
		}
	}

	case class UtilsCollection(name: String, description: String, utilClasses: Class[_]*)

	case class Lang(name: String, arrayType: String, utilClassName: Class[_] ⇒ String) {
		def mapType(cls: Class[_]): String = {
			if (cls.isArray) {
				arrayType
			} else {
				cls match {
					case ShortT ⇒ "SHORT"
					case IntT ⇒ "INT"
					case LongT ⇒ "LONG"
					case FloatT ⇒ "FLOAT"
					case DoubleT ⇒ "DOUBLE"
					case CharT ⇒ "CHAR"
					case ByteT ⇒ "BYTE"
					case BooleanT ⇒ "BOOLEAN"
					case s ⇒ s.getCanonicalName
				}
			}
		}

		val classMethodExcludes: Set[(String, String)] = {
			val url = getClass.getResource(s"$name.excludes")

			if (url == null) {
				Set()
			} else {
				managed(Source.fromURL(url, "UTF-8")).acquireAndGet(_.getLines()
					.filterNot(_.isEmpty)
					.map(_.split("→") match { case Array(a, b) ⇒ (a.trim, b.trim) })
					.toSet
				)
			}
		}
	}

	private val ShortT   = classOf[Short]
	private val IntT     = classOf[Int]
	private val LongT    = classOf[Long]
	private val FloatT   = classOf[Float]
	private val DoubleT  = classOf[Double]
	private val CharT    = classOf[Char]
	private val ByteT    = classOf[Byte]
	private val BooleanT = classOf[Boolean]

}

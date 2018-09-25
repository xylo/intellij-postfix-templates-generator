package de.endrullis.idea.postfixtemplatesgenerator

import org.apache.commons.io.IOUtils

/**
 * @author Stefan Endrullis
 */
object App {

	def main(args: Array[String]) {
		val java = false

		val arrayType = if (java) "ARRAY" else "scala.Array"

		val utilClass = classOf[IOUtils]
		val allMethods = utilClass.getDeclaredMethods.toList.filter(m ⇒ m.getParameterCount > 0 && m.getAnnotation(classOf[Deprecated]) == null)

		allMethods.groupBy(_.getName).foreach{case (name, methods) ⇒
			println("." + name + " : " + name)
			methods.groupBy(_.getParameterTypes.head).foreach{case (firstType, ms) =>
				val matchingType = if (firstType.isArray) arrayType else firstType.getCanonicalName

				val params = ms.filter(_.getParameterCount > 1).map(_.getParameterTypes()(1)).toSet

				val className = utilClass.getCanonicalName
				val utilClassName = if (java) utilClass.getCanonicalName else utilClass.getCanonicalName.replaceFirst(".*\\.", "")

				val leftSide = s"$matchingType [$className]"

				val rightSide = if (params.isEmpty) {
					s"$utilClassName.$name($$expr$$)"
				} else {
					s"$utilClassName.$name($$expr$$, $$arg$$)"
				}

				println(s"\t$leftSide  →  $rightSide")
			}
			println()
		}
	}

}

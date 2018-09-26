package de.endrullis.idea.postfixtemplatesgenerator

import java.io.File

import scala.io.Source

/**
 * Small tool that counts the number of templates and template rules.
 *
 * @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
 */
object PostfixTemplateCounter extends App {

	val templateDir = new File("../intellij-postfix-templates/templates/")

	val counts = for (langDir ← templateDir.listFiles(_.isDirectory);
	                  file ← langDir.listFiles(_.getName.endsWith(".postfixTemplates")))
		           yield getCount(file)
	
	val count = counts.reduce(_ + _)

	println("count = " + count)

	def getCount(file: File): Count = {
		val lines = Source.fromFile(file, "UTF-8").getLines()
			.map(_.trim)
			.filterNot(l ⇒ l.isEmpty || l.startsWith("#"))
			.toList

		Count(lines.count(_.startsWith(".")), lines.count(_.contains("→")))
	}

	case class Count(noTemplates: Int, noRules: Int) {
		def + (that: Count) = Count(this.noTemplates + that.noTemplates, this.noRules + that.noRules)
	}

}

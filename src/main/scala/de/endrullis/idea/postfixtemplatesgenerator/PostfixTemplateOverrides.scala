package de.endrullis.idea.postfixtemplatesgenerator

import java.io.File

import scala.io.Source

/**
 * Small tool that determines template overrides.
 *
 * @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
 */
object PostfixTemplateOverrides extends App {

	val TemplateName = raw"""\.([\w]+)\s.*""".r
	val MatchingType = raw"""\s*([^→]+?)\s*→.*""".r
	val NoMatchingType = raw"""[^→]*""".r

	val templateDir = new File("../intellij-postfix-templates/templates/")

	val counts = for (langDir ← templateDir.listFiles(_.isDirectory))
		           yield determineOverrides(langDir.listFiles(_.getName.endsWith(".postfixTemplates")).toList)

	def determineOverrides(files: List[File]) = {
		val conflicts = files.flatMap(determineTemplates).groupBy(tt ⇒ (tt.template, tt.matchingType)).filter(_._2.size > 1).mapValues(_.map(_.file.getName))

		for (((template, matchingType), files) ← conflicts) {
			println(s"$template -> $matchingType: $files")
		}
	}

	def determineTemplates(file: File) = {
		val lines = Source.fromFile(file, "UTF-8").getLines()
			.map(_.trim)
			.filterNot(l ⇒ l.isEmpty || l.startsWith("#"))
			.toList

		var templateName: Option[String] = None

		lines.map {
			case TemplateName(name) ⇒
				templateName = Some(name)
				None
			case MatchingType(matchingType) ⇒
				Some(TT(file, templateName.get, matchingType))
			case NoMatchingType() ⇒
				None
		}.collect{case Some(tt) ⇒ tt}
	}

	case class TT(file: File, template: String, matchingType: String)

}

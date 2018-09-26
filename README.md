# Template file generator for Custom Postfix Templates
This tool simplifies and automates the generation of [Custom Postfix Tempaltes](https://github.com/xylo/intellij-postfix-templates) for Intellij IDEA.  It scans utility classes and generates postfix templates for its static methods.

## Background

### Task

We would like to extend an existing type with a new method, e.g.

    String.isNumeric

### Workaround

Since Java does not allow libraries to extend existing types the common solution is to write a utility class containing the new method which takes as first parameter the source object, e.g.

    public class StringUtils {
      public static boolean isNumeric(String s) {...}
    }

However, to apply this function to your string object you need to be aware of the existence of this function and you need to know in which utility class it is located.  This is combersome and unintuitive.

### Solution: Postfix Template

Using the [Custom Postfix Tempaltes](https://github.com/xylo/intellij-postfix-templates) plugin of Intellij IDEA we can actually define postfix templates that allow us to write

    "foo".isNumeric

and expand this expression to

    StringUtils.isNumeric("foo")
    
### Automatic Template File Generation

Instead of writing all postfix templates by hand this tool can automate most of this process for you.
Just add your library as dependency to the `pom.xml`.
Then open the Scala file `PostfixTemplateGenerator` and add your `UtilsCollection` configuration to the list `utilsCollections`.
Then run `PostfixTemplateGenerator` and your templates file will be generated to Java and Scala.

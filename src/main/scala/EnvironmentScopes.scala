import scala.collection.mutable
import FuzzyExpressions.FuzzyExpression
import FuzzyExpressions.FuzzyExpression._

object EnvironmentScopes:

  // The Environment class
  class Environment(
                     val name: Option[String],
                     val variables: mutable.Map[String, FuzzyExpression],
                     val parent: Option[Environment] = None,
                     val children: mutable.ListBuffer[Environment] = mutable.ListBuffer.empty
                   ):
    // Method to look up a variable, recursively checking parent environments
    def lookup(name: String): Option[FuzzyExpression] =
      variables.get(name).orElse(parent.flatMap(_.lookup(name)))

    // Method to set or update a variable in the current environment
    def setVariable(name: String, value: FuzzyExpression): Unit =
      if variables.contains(name) then
        variables(name) = value
      else
        // If the variable doesn't exist in the current scope, add it here
        variables(name) = value

    // Method to create a new child environment with an optional name
    def createChild(childName: Option[String] = None): Environment =
      val child = new Environment(childName, mutable.Map.empty, Some(this))
      children += child
      child

    // Method to get or create a new child environment with an optional name
    def getOrCreateChild(childName: Option[String] = None): Environment =
      // Check if a child with the given name already exists
      children.find(_.name == childName) match
        case Some(existingChild) => existingChild
        case None =>
          // If no child exists, create a new one
          val newChild = new Environment(childName, mutable.Map.empty, Some(this))
          children += newChild
          newChild

    // Method to find a scope by its name (searches current and child scopes)
    def findScope(scopeName: String): Option[Environment] =
      if name.contains(scopeName) then
        Some(this)
      else
        // Recursively search in child environments
        children.view.flatMap(_.findScope(scopeName)).headOption

    // Method to find a scope by a path of scope names
    def findScopeByPath(path: List[String]): Option[Environment] = path match
      case Nil => Some(this)
      case head :: tail =>
        // Find the child with the matching name and continue the search
        children.find(_.name.contains(head)).flatMap(_.findScopeByPath(tail))

    // Method to print the current environment and its variables (for debugging)
    def printEnvironment(indent: Int = 0): Unit =
      val indentation = "  " * indent
      val scopeName = name.getOrElse("Unnamed Scope")
      println(s"$indentation Scope: $scopeName")
      println(s"$indentation Variables: ${variables.map { case (k, v) => s"$k -> $v" }.mkString(", ")}")
      children.foreach(_.printEnvironment(indent + 1))
package articles

import pl.metastack.metadocs.SectionSupport

object FunctorAdts extends SectionSupport {
  sectionNoExec("functor") {
    trait F[A] {
      def map[B](f: A => B): F[B]
    }
  }

  sectionNoExec("bin-tree") {
    sealed trait Tree
    case class Leaf(value: Int) extends Tree
    case class Branch(left: Tree, right: Tree) extends Tree

    Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
  }

  sectionNoExec("bin-tree-map") {
    sealed trait Tree {
      /** Recursively apply `f` to all children */
      def map(f: Tree => Tree): Tree
    }
    case class Leaf(value: Int) extends Tree {
      def map(f: Tree => Tree): Leaf = this
    }
    case class Branch(left: Tree, right: Tree) extends Tree {
      def map(f: Tree => Tree): Branch = Branch(f(left).map(f), f(right).map(f))
    }

    val tree = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    tree.map(identity): Branch
  }

  sectionNoExec("html-tree") {
    sealed trait Node {
      def map(f: Node => Node): Node
    }
    case class Text(value: String) extends Node {
      def map(f: Node => Node): Text = this
    }
    sealed trait Tag extends Node {
      def children: Seq[Node]
      def copy(children: Seq[Node]): Tag
      def map(f: Node => Node): Tag = copy(children.map(f(_).map(f)))
    }
    case class Div(id: Option[String], children: Node*) extends Tag {
      def copy(children: Seq[Node]): Div = Div(id, children: _*)
    }
    case class B(children: Node*) extends Tag {
      def copy(children: Seq[Node]): B = B(children: _*)
    }

    val div = Div(None, B(Text("Hello")))
    div.map(identity): Tag
  }

  sectionNoExec("type-param") {
    sealed trait Node[T] {
      def map(f: Node[_] => Node[_]): T
    }
    case class Text(value: String) extends Node[Text] {
      def map(f: Node[_] => Node[_]): Text = this
    }
    sealed trait Tag[T <: Tag[_]] extends Node[T] {
      def children: Seq[Node[_]]
      def copy(children: Seq[Node[_]]): T
      def map(f: Node[_] => Node[_]): T =
        copy(children.map(f(_).map(f).asInstanceOf[Node[_]]))
    }
    case class Div(id: Option[String], children: Node[_]*) extends Tag[Div] {
      def copy(children: Seq[Node[_]]): Div = Div(id, children: _*)
    }

    val div = Div(None, Text("Hello"))
    val mapped: Div = div.map(identity)
  }

  sectionNoExec("type-member") {
    sealed trait Node {
      type T <: Node
      def map(f: Node => Node): T
    }
    case class Text(value: String) extends Node {
      override type T = Text
      def map(f: Node => Node): Text = this
    }
    sealed trait Tag extends Node {
      override type T <: Tag
      def children: Seq[Node]
      def copy(children: Seq[Node]): T
      def map(f: Node => Node): T = copy(children.map(f(_).map(f)))
    }
    case class Div(id: Option[String], children: Node*) extends Tag {
      override type T = Div
      def copy(children: Seq[Node]): Div = Div(id, children: _*)
    }

    val div = Div(None, Text("Hello"))
    div.map(identity): Div
  }
}

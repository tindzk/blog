package articles

import pl.metastack.metadocs.SectionSupport

object TypePolymorphism extends SectionSupport {
  sectionNoExec("problem") {
    sealed trait Base {
      def change: Base
    }
    case class Child() extends Base {
      override def change: Base = this
    }

    Child().change: Base
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
      override def map(f: Tree => Tree): Leaf = this
    }
    case class Branch(left: Tree, right: Tree) extends Tree {
      override def map(f: Tree => Tree): Branch =
        Branch(f(left).map(f), f(right).map(f))
    }

    val tree = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    tree.map(identity): Branch
  }

  sectionNoExec("html-tree") {
    sealed trait Node {
      def map(f: Node => Node): Node
    }
    case class Text(value: String) extends Node {
      override def map(f: Node => Node): Text = this
    }
    sealed trait Tag extends Node {
      def children: Seq[Node]
      def withChildren(children: Seq[Node]): Tag
      override def map(f: Node => Node): Tag =
        withChildren(children.map(f(_).map(f)))
    }
    case class Div(id: Option[String],
                   children: Node*) extends Tag {
      override def withChildren(children: Seq[Node]): Div =
        Div(id, children: _*)
    }
    case class B(children: Node*) extends Tag {
      override def withChildren(children: Seq[Node]): B =
        B(children: _*)
    }

    val div = Div(None, B(Text("Hello")))
    div.map(identity): Tag
  }

  sectionNoExec("type-param") {
    sealed trait Node[T] {
      def map(f: Node[_] => Node[_]): T
    }
    case class Text(value: String) extends Node[Text] {
      override def map(f: Node[_] => Node[_]): Text = this
    }
    sealed trait Tag[T <: Tag[_]] extends Node[T] {
      def children: Seq[Node[_]]
      def withChildren(children: Seq[Node[_]]): T
      override def map(f: Node[_] => Node[_]): T =
        withChildren(children.map(f(_).map(f)
          .asInstanceOf[Node[_]]))
    }
    case class Div(id: Option[String],
                   children: Node[_]*) extends Tag[Div] {
      override def withChildren(children: Seq[Node[_]]): Div =
        Div(id, children: _*)
    }

    val div = Div(None, Text("Hello"))
    val mapped: Div = div.map(identity)
  }

  sectionNoExec("this-type") {
    sealed trait Node {
      def map(f: Node => Node): this.type
    }
    case class Text(value: String) extends Node {
      override def map(f: Node => Node): this.type = this
    }
    sealed trait Tag extends Node {
      def children: Seq[Node]
      def withChildren(children: Seq[Node]): this.type
      override def map(f: Node => Node): this.type =
        withChildren(children.map(f(_).map(f)))
    }
    case class Div(id: Option[String],
                   children: Node*) extends Tag {
      override def withChildren(children: Seq[Node]): this.type =
        Div(id, children: _*).asInstanceOf[this.type]
    }

    val div = Div(None, Text("Hello"))
    div.map(identity): Div
  }

  sectionNoExec("this-type-parameterised") {
    trait MySeq[T] {
      def map[U](f: T => U): this.type
    }

    class MyList[T] extends MySeq[T] {
      override def map[U](f: T => U): this.type = ???
    }

    class MyVector[T] extends MySeq[T] {
      override def map[U](f: T => U): this.type = ???
    }
  }

  sectionNoExec("type-member") {
    sealed trait Node {
      type T <: Node
      def map(f: Node => Node): T
    }
    case class Text(value: String) extends Node {
      override type T = Text
      override def map(f: Node => Node): Text = this
    }
    sealed trait Tag extends Node {
      override type T <: Tag
      def children: Seq[Node]
      def withChildren(children: Seq[Node]): T
      override def map(f: Node => Node): T =
        withChildren(children.map(f(_).map(f)))
    }
    case class Div(id: Option[String],
                   children: Node*) extends Tag {
      override type T = Div
      override def withChildren(children: Seq[Node]): Div =
        Div(id, children: _*)
    }

    val div = Div(None, Text("Hello"))
    div.map(identity): Div
  }

  sectionNoExec("type-member-covariance") {
    trait MySeq[T] {
      type Child[T] <: MySeq[T]
      def map[U](f: T => U): Child[U]
    }

    class MyList[T] extends MySeq[T] {
      override type Child[T] = MyList[T]
      override def map[U](f: T => U): Child[U] = new MyList[U]
    }

    class MyVector[T] extends MySeq[T] {
      override type Child[T] = MyVector[T]
      override def map[U](f: T => U): Child[U] = new MyVector[U]
    }
  }
}

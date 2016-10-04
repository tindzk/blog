import java.io.File

import org.eclipse.jgit.api.Git
import org.joda.time.DateTime

import pl.metastack.metaweb._
import pl.metastack.{metaweb => web}

import pl.metastack.metadocs.input._
import pl.metastack.metadocs.document._
import pl.metastack.metadocs.document.tree.Post
import pl.metastack.metadocs.input.metadocs._
import pl.metastack.metadocs.output.html.Components
import pl.metastack.metadocs.output.html.document

object Generate extends App {
  import Globals._

  if (!repoPath.exists())
    Git.cloneRepository()
      .setURI(s"git@github.com:$userName/$repoName.git")
      .setDirectory(repoPath)
      .call()

  repoPath
    .listFiles()
    .filter(_.getPath.endsWith(".html"))
    .foreach(_.delete())

  val TwitterHandle = "timnieradzik"
  val TwitterUrl = "https://twitter.com/intent/tweet?text="

  val meta = Meta(
    date = DateTime.now(),
    title = "tindzk's blog",
    author = "Tim Nieradzik",
    affiliation = "MetaStack",
    `abstract` = "Blog about Scala and Machine Learning",
    language = "en-GB",
    url = "http://nieradzik.me/",
    avatar = Some("images/avatar.png"),
    editSourceURL = Some("https://github.com/tindzk/blog/edit/master/"))

  def navigation(meta: Meta, index: Boolean): web.tree.Node = {
    val navigation = html("templates/navigation.html")
    if (!index) navigation
    else navigation.set(navigation.children.flatMap {
      case n: web.tree.Tag if n.id.contains("index") => Seq.empty
      case n => Seq(n)
    })
  }

  def share(meta: Meta, post: Post): web.tree.Node = {
    val tweet = s"${document.Blog.postUrl(meta, post)} - ${post.title} by @$TwitterHandle"
    val href = TwitterUrl + tweet

    html("templates/share.html")
      .updateChild[web.tag.A]("edit", _.href(
        meta.editSourceURL.get + post.sourcePath.get))
      .asInstanceOf[web.tree.Tag]
      .updateChild[web.tag.A]("twitter", _.href(href))
  }

  def profile(meta: Meta): web.tree.Node =
    html("templates/profile.html")
      .instantiate(
        "author"      -> web.tree.Text(meta.author),
        "description" -> web.tree.Text(meta.`abstract`))
      .updateChild[web.tag.Img]("avatar", _.src(meta.avatar.get))

  val files = new File("articles")
    .listFiles()
    .map(_.getPath)
    .filter(_.endsWith(".txt"))
    .sorted

  val instructionSet = DefaultInstructionSet
    .inherit(CodeInstructionSet)
    .inherit(BlogInstructionSet)
    .inherit(DraftInstructionSet)
    .withAliases(
      "b" -> Bold,
      "i" -> Italic,
      "item" -> ListItem)

  val rawTrees = files.flatMap(file =>
    MetaDocs.loadFile(file,
      instructionSet,
      generateId = caption => Some(caption.collect {
        case c if c.isLetterOrDigit => c
        case c if c.isSpaceChar => '-'
      }.toLowerCase
    )) match {
      case Left(se) =>
        println("File: " + file)
        println(se)
        None
      case Right(r) => Some(r)
    })

  val docTree = Document.mergeTrees(rawTrees)

  Document.printTodos(docTree)

  val pipeline =
    Document.pipeline
      .andThen(CodeProcessor.embedListings(".") _)
      .andThen(CodeProcessor.embedOutput)
  val docTreeWithCode = pipeline(docTree)

  val skeleton = Components.pageSkeleton(
    cssPaths = Seq(
      "css/style.css",
      "css/highlight.css"
    ),
    jsPaths = Seq(
      "//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js",
      "js/main.js",
      "js/highlight.js"
    ),
    script = Some("hljs.initHighlightingOnLoad();"),
    favicon = Some("images/favicon.ico"),
    rss = Some("posts.xml")
  )(_, _, _)

  document.Blog.write(
    root = docTreeWithCode,
    skeleton = skeleton,
    pageFooter = Some(Components.generatedWith(true)),
    indexHeader = Some(navigation(meta, index = true)),
    indexBodyHeader = Some(profile(meta)),
    postHeader = Some(navigation(meta, index = false)),
    postFooter = Some(share(meta, _: Post)),
    outputPath = repoPath.getPath,
    meta = meta)
}

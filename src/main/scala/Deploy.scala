import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

object Deploy extends App {
  import Globals._

  val repo = FileRepositoryBuilder.create(new File(repoPath, ".git"))
  val git = new Git(repo)
  git.add().addFilepattern(".").call()
  if (!git.status().call().isClean) {
    git.commit()
      .setAll(true)
      .setMessage(s"Update").call()
    git.push().call()
  }
}

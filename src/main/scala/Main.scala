package info.schleichardt.restartonedit

import scala.sys.process._
import org.apache.commons.vfs2._
import org.apache.commons.vfs2.impl.DefaultFileMonitor
import org.apache.commons.lang.SystemUtils

class Listener(block:() => Any) extends FileListener {
  override def fileCreated(event: FileChangeEvent) {
    block()
    ()
  }

  override def fileDeleted(event: FileChangeEvent) {
    block()
    ()
  }

  override def fileChanged(event: FileChangeEvent) {
    block()
    ()
  }

}

class Runner(command: String) {
  private var process: Option[Process] = None
  def restart() = {
    stop()
    start()
  }

  def start() = {process = Option(command.run); this}
  def stop() = {process.map(_.destroy()); this}
}

object Main extends App {


    val fsManager = VFS.getManager()
    val watchDir = fsManager.resolveFile(SystemUtils.getUserDir(), ".")
    val runner = new Runner("unison").start()
    val taskOnFileChange: () => Any = () => runner.restart()
    val fm = new DefaultFileMonitor(new Listener(taskOnFileChange))
    fm setRecursive true
    fm addFile watchDir
  try {
    fm start()
    println("press enter to terminate")
    val ln = readLine()
  } finally {
    fm.stop()
    runner.stop()
  }
}

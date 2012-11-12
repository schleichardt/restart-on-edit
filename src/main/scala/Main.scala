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
  if (args.length < 1) {
    println("specify a command")
  } else {
    val runner = new Runner(args.mkString(" ")).start()
    val taskOnFileChange: () => Any = () => runner.restart()
    val fm = setupFileMonitor(new Listener(taskOnFileChange))

    try {
      fm start()
      println("press enter to terminate")
      val ln = readLine()
    } finally {
      fm.stop()
      runner.stop()
    }
  }

  def setupFileMonitor(listener: FileListener): DefaultFileMonitor = {
    val fsManager = VFS.getManager()
    val watchDir = fsManager.resolveFile(SystemUtils.getUserDir(), ".")
    val fm = new DefaultFileMonitor(listener)
    fm setRecursive true
    fm addFile watchDir
    fm
  }
}

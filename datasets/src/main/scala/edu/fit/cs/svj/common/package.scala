package edu.fit.cs.svj

import java.io.File
import System.{ currentTimeMillis => _time }

package object common {

  /**
   * Get a child of a file. For example,
   *
   * 	subFile(homeDir, "b", "c")
   *
   * corresponds to ~/b/c
   */
  def subFile(file: File, children: String*) = children.foldLeft(file)((file, child) => new File(file, child))

  /**
   * Get a resource from the 'src/main/resources' directory. Eclipse does not copy
   * resources to the output directory, then the class loader cannot find them
   */
  def resourceAsStreamFromSrc(resourcePath: List[String]): Option[java.io.InputStream] = {
    val classesDir = new File(getClass.getResource(".").toURI)
    val projectDir = classesDir.getParentFile.getParentFile.getParentFile.getParentFile
    val resourceFile = subFile(projectDir, ("src" :: "main" :: "resources" :: resourcePath): _*)
    if (resourceFile.exists) Some(new java.io.FileInputStream(resourceFile)) else None
  }

  def absPath(resource: String) = getClass.getResource(resource).getPath

  def profile[R](block: => R, t: Long = _time) = (block, _time - t)

  def toXML(data: Map[(String, Long), List[(Int, Int)]]) =
    <comparison>
      {
        data.map {
          case ((name, time), values) =>
            <execution name={ name } time={ time.toString }>
              {
                values.sortBy(_._1).map {
                  case (k, v) => <item id={ (k + 1).toString } detected={ v.toString }/>
                }
              }
            </execution>
        }
      }
    </comparison>

}
package edu.fit.cs.svj.model

import scala.xml.XML

case class Cascade(name: String, windowSize: (Int, Int), stages: Seq[Stage])

object Cascade {
  
  def apply(xmlFile: String) = {
    val root = XML.loadFile(xmlFile)
    val cascadeNode = root.child.filterNot(_.label.equals("#PCDATA")).head
    val Array(x, y) = (cascadeNode  \ "size").head.text split " "
    val name = cascadeNode.label
    val stages = (cascadeNode \ "stages" \ "_").filterNot(_.label.equals("#PCDATA")) map { stage =>
      val stageTh = (stage \ "stage_threshold").headOption map (_.text.toDouble)
      val parent = (stage \ "parent").headOption map (_.text.toInt)
      val next = (stage \ "next").headOption map (_.text.toInt)
      val classifiers = (stage \ "trees" \ "_") map { classifier => 
        val features = (classifier \ "_") map { feature =>
          val th = (feature \ "threshold").headOption map (_.text.toDouble)
          val lv = (feature \ "left_val").headOption map (_.text.toDouble)
          val ln = (feature \ "left_node").headOption map (_.text.toInt)
          val rv = (feature \ "right_val").headOption map (_.text.toDouble)
          val rn = (feature \ "right_node").headOption map (_.text.toInt)
          val tilted = (feature \ "feature" \ "tilted").head map (_.text) match {
            case s => if (s.equals("0")) false else true
          }
          val rectangles = (feature \ "feature" \ "rects" \ "_") map (r => Rectangle(r.text, " "))
          
          new Feature(rectangles, tilted, th.get, lv, ln, rv, rn)
        }
        
        new Classifier(features)
      }
      
      new Stage(classifiers, stageTh, parent.get, next.get)
    }
    
    new Cascade(name, (x.toInt, y.toInt), stages)
  }
}
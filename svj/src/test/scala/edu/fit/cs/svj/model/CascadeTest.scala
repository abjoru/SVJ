package edu.fit.cs.svj.model

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.fit.cs.svj.data.Datasets

@RunWith(classOf[JUnitRunner])
class CascadeTest extends FunSuite {
  import edu.fit.cs.svj.common._
  
  test("Cascade 'frontal face default' parses correctly") {
    val cascade = Cascade(absPath(Datasets.HAAR_FF_DEF_FILE))
    
    assert(cascade.name == "haarcascade_frontalface_default")
    assert(cascade.windowSize == (24, 24))
    assert(cascade.stages.size == 25)
    
    // sample first stage
    val stage = cascade.stages.head
    assert(stage.threshold.get === -5.0425500869750977d)
    assert(stage.parentIndex === -1)
    assert(stage.nextIndex === -1)
    assert(stage.classifiers.size === 9)
    
    // sample first classifier
    val classifier = stage.classifiers.head
    assert(classifier.features.size === 1)
    
    // sample first feature
    
    val feature = classifier.features.head
    assert(feature.threshold === -0.0315119996666908d)
    assert(feature.leftVal.get === 2.0875380039215088d)
    assert(feature.rightVal.get === -2.2172100543975830)
    assert(feature.leftNode.isEmpty)
    assert(feature.rightNode.isEmpty)
    assert(feature.rectangles.head.x === 6)
    assert(feature.rectangles.head.y === 4)
    assert(feature.rectangles.head.width === 12)
    assert(feature.rectangles.head.height === 9)
    assert(feature.rectangles.head.weight === -1d)
  }
  
  test("Cascade 'frontal face alt2' parses correctly") {
    val cascade = Cascade(absPath(Datasets.HAAR_FF_ALT2_FILE))
    
    assert(cascade.name == "haarcascade_frontalface_alt2")
    assert(cascade.windowSize == (20, 20))
    assert(cascade.stages.size == 20)
    
    // sample first stage
    val stage = cascade.stages.head
    assert(stage.threshold.get === 0.3506923019886017)
    assert(stage.parentIndex === -1)
    assert(stage.nextIndex === -1)
    assert(stage.classifiers.size === 3, "classifiers size did not equal 3")
    
    // sample first classifier
    val classifier = stage.classifiers.head
    assert(classifier.features.size === 2)
    
    
    // sample first feature
    val feature = classifier.features.head
    assert(feature.threshold === 4.3272329494357109e-003)
    assert(feature.leftVal.get === 0.0383819006383419)
    assert(feature.rightVal.isEmpty)
    assert(feature.leftNode.isEmpty)
    assert(feature.rightNode.get === 1)
    assert(feature.rectangles.head.x === 2)
    assert(feature.rectangles.head.y === 7)
    assert(feature.rectangles.head.width === 16)
    assert(feature.rectangles.head.height === 4)
    assert(feature.rectangles.head.weight === -1d)
  }

}
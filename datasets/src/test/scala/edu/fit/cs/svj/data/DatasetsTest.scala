package edu.fit.cs.svj.data

import org.scalatest.FunSuite
import java.io.File

class DatasetsTest extends FunSuite {
  
  test("ORL Faces csv can be resolved") {
    assert(Datasets.orlFaces != null)
  }
  
  test("HAAR Cascade frontal face alt can be resolved") {
    assert(Datasets.haarCascadeFrontalFaceAlt != null)
  }
  
  test("HAAR Cascade frontal face alt2 can be resolved") {
    assert(Datasets.haarCascadeFrontalFaceAlt2 != null)
  }
  
  test("ORL Dataset group") {
    assert(Datasets.groups.size > 0)
    Datasets.groups.keys foreach (k => assert(Datasets.groups(k).size == 10))
  }

}
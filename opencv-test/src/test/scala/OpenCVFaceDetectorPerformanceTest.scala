import org.scalameter.PerformanceTest
import org.opencv.core.Core
import org.scalameter.Gen
import org.scalameter.persistence.SerializationPersistor
import org.scalameter.Executor
import org.scalameter.Aggregator
import org.scalameter.execution.LocalExecutor
import org.scalameter.Persistor
import org.scalameter.Reporter
import org.scalameter.reporting.RegressionReporter
import org.scalameter.reporting.HtmlReporter
import edu.fit.cs.svj.opencv.TestRunner

class OpenCVFaceDetectorPerformanceTest {//extends PerformanceTest {
  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  def warmer = Executor.Warmer.Default()
  def aggregator = Aggregator.complete(Aggregator.average)
  def measurer = new Executor.Measurer.Default()
  def executor = new LocalExecutor(warmer, aggregator, measurer)
  def persistor = Persistor.None
  def reporter: Reporter = Reporter.Composite(
      new RegressionReporter(RegressionReporter.Tester.OverlapIntervals(), RegressionReporter.Historian.ExponentialBackoff()),
      HtmlReporter(true)
  )
  
  val data = Gen.single("image")("lena.png")
  /*
  performance of "OpenCV" in {
    measure method "detect" in {
      using(data) in { img =>
        OpenCVFaceDetector.detect("lbpcascade_frontalface.xml", img)
      }
    }
  }*/
}
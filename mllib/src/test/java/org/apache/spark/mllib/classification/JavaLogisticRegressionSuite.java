/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.mllib.classification;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

public class JavaLogisticRegressionSuite implements Serializable {
  private transient SparkSession spark;
  private transient JavaSparkContext jsc;

  @Before
  public void setUp() {
    spark = SparkSession.builder()
      .master("local")
      .appName("JavaLogisticRegressionSuite")
      .getOrCreate();
    jsc = new JavaSparkContext(spark.sparkContext());
  }

  @After
  public void tearDown() {
    spark.stop();
    spark = null;
  }

  int validatePrediction(List<LabeledPoint> validationData, LogisticRegressionModel model) {
    int numAccurate = 0;
    for (LabeledPoint point : validationData) {
      Double prediction = model.predict(point.features());
      if (prediction == point.label()) {
        numAccurate++;
      }
    }
    return numAccurate;
  }

  @Test
  public void runLRUsingConstructor() {
    int nPoints = 10000;
    double A = 2.0;
    double B = -1.5;

    JavaRDD<LabeledPoint> testRDD = jsc.parallelize(
      LogisticRegressionSuite.generateLogisticInputAsList(A, B, nPoints, 42), 2).cache();
    List<LabeledPoint> validationData =
      LogisticRegressionSuite.generateLogisticInputAsList(A, B, nPoints, 17);

    LogisticRegressionWithSGD lrImpl = new LogisticRegressionWithSGD();
    lrImpl.setIntercept(true);
    lrImpl.optimizer().setStepSize(1.0)
      .setRegParam(1.0)
      .setNumIterations(100);
    LogisticRegressionModel model = lrImpl.run(testRDD.rdd());

    int numAccurate = validatePrediction(validationData, model);
    Assert.assertTrue(numAccurate > nPoints * 4.0 / 5.0);
  }

  @Test
  public void runLRUsingStaticMethods() {
    int nPoints = 10000;
    double A = 0.0;
    double B = -2.5;

    JavaRDD<LabeledPoint> testRDD = jsc.parallelize(
      LogisticRegressionSuite.generateLogisticInputAsList(A, B, nPoints, 42), 2).cache();
    List<LabeledPoint> validationData =
      LogisticRegressionSuite.generateLogisticInputAsList(A, B, nPoints, 17);

    LogisticRegressionModel model = LogisticRegressionWithSGD.train(
      testRDD.rdd(), 100, 1.0, 1.0);

    int numAccurate = validatePrediction(validationData, model);
    Assert.assertTrue(numAccurate > nPoints * 4.0 / 5.0);
  }
}

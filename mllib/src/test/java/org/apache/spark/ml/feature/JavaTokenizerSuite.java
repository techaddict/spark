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

package org.apache.spark.ml.feature;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class JavaTokenizerSuite {
  private transient SparkSession spark;
  private transient JavaSparkContext jsc;

  @Before
  public void setUp() {
    spark = SparkSession.builder()
      .master("local")
      .appName("JavaTokenizerSuite")
      .getOrCreate();
    jsc = new JavaSparkContext(spark.sparkContext());
  }

  @After
  public void tearDown() {
    spark.stop();
    spark = null;
  }

  @Test
  public void regexTokenizer() {
    RegexTokenizer myRegExTokenizer = new RegexTokenizer()
      .setInputCol("rawText")
      .setOutputCol("tokens")
      .setPattern("\\s")
      .setGaps(true)
      .setToLowercase(false)
      .setMinTokenLength(3);


    JavaRDD<TokenizerTestData> rdd = jsc.parallelize(Arrays.asList(
      new TokenizerTestData("Test of tok.", new String[]{"Test", "tok."}),
      new TokenizerTestData("Te,st.  punct", new String[]{"Te,st.", "punct"})
    ));
    Dataset<Row> dataset = spark.createDataFrame(rdd, TokenizerTestData.class);

    List<Row> pairs = myRegExTokenizer.transform(dataset)
      .select("tokens", "wantedTokens")
      .collectAsList();

    for (Row r : pairs) {
      Assert.assertEquals(r.get(0), r.get(1));
    }
  }
}

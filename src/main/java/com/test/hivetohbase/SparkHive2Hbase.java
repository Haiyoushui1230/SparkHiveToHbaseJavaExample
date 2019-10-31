package com.test.hivetohbase;

import com.test.utils.HbaseWriter;
import com.test.utils.IDUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import java.util.Iterator;

public class SparkHive2Hbase {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaSparkHive")
                .enableHiveSupport()
                .getOrCreate();
        Dataset<Row> sql = spark.sql("SELECT * FROM psn");

        sql.show();
        sql.javaRDD().repartition(3).foreachPartition(new VoidFunction<Iterator<Row>>() {
            @Override
            public void call(Iterator<Row> rowIterator) throws Exception {
                while (rowIterator.hasNext()){
                     Row next = rowIterator.next();
                     String id = next.getAs("id");
                    String name = next.getAs("name");
                    String likes = next.getAs("likes");
                    String addres = next.getAs("addres");

                    String md5Start16 = IDUtils.getMd5Value(id).substring(0,16);
                    System.out.println(md5Start16);
                    byte[] rk = Bytes.toBytes(String.format("%02d_%s", IDUtils.shardId(IDUtils.hash(md5Start16), 10), md5Start16));

                    Put put = new Put(rk);

                    put.addColumn("cf".getBytes(),"id".getBytes(),id.getBytes());
                    put.addColumn("cf".getBytes(),"name".getBytes(),name.getBytes());
                    put.addColumn("cf".getBytes(),"likes".getBytes(),likes.getBytes());
                    put.addColumn("cf".getBytes(),"addres".getBytes(),addres.getBytes());

                    //使用BufferedMutator，50M写一次
                    HbaseWriter.getInstance("table_name").mutate(put);

                }
                HbaseWriter.getInstance("table_name").flush();

            }

        });

    }
}

package com.test.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HbaseWriter {
    private  static Map<String,HbaseWriter> instance = new ConcurrentHashMap();

    private BufferedMutator bm;

    private HbaseWriter(String table)throws IOException{
        TableName outTableName = TableName.valueOf(table);
        ExecutorService pool = Executors.newFixedThreadPool(5);
        Configuration hbconf = HBaseConfiguration.create();
        hbconf.set("","");
        hbconf.set("","");
        hbconf.set("","");
         Connection connection = ConnectionFactory.createConnection(hbconf);
        BufferedMutatorParams bm_para = new BufferedMutatorParams(outTableName).writeBufferSize(50*1024*1024).pool(pool);
        bm = connection.getBufferedMutator(bm_para);
    }

    public static HbaseWriter getInstance(String table) throws  IOException{
        if (instance.get(table)==null){
            synchronized (HbaseWriter.class){
                if (instance.get(table)==null){
                    //System.out.println("create new ");
                    instance.put(table,new HbaseWriter(table));
                }
            }
        }
        return instance.get(table);
    }
    public void flush(){
        try {
            bm.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void mutate(Mutation mutation) throws IOException {
        bm.mutate(mutation);
    }
    public void mutate(List<Mutation> mutations) throws IOException {
        bm.mutate(mutations);
    }
}

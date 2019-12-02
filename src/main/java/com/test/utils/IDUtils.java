package com.test.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hbase.util.Bytes;
import java.util.Random;


public class IDUtils {

    /**
     * 图片名生成
     */
    public static String genImageName() {
        //取当前时间的长整形值包含毫秒
        long millis = System.currentTimeMillis();
        //long millis = System.nanoTime();
        //加上三位随机数
        Random random = new Random();
        int end3 = random.nextInt(999);
        //如果不足三位前面补0
        String str = millis + String.format("%03d", end3);

        return str;
    }

    /**
     * 商品id生成
     */
    public static long genItemId() {
        //取当前时间的长整形值包含毫秒
        long millis = System.currentTimeMillis();
        //long millis = System.nanoTime();
        //加上两位随机数
        Random random = new Random();
        int end2 = random.nextInt(99);
        //如果不足两位前面补0
        String str = millis + String.format("%02d", end2);
        long id = new Long(str);
        return id;
    }

    /**
     * 根据指定文档ID计算整型哈希值
     *
     * @param docId 文档ID
     * @return 指定文档ID对应的整型哈希值
     * @throws NullPointerException 如果docId为null则抛出此异常
     */
    public static int hash(String docId) {
        if (docId == null) {
            throw new NullPointerException();
        }
        final byte[] data = new byte[docId.length() * 2];
        for (int i = 0; i < docId.length(); ++i) {
            final char c = docId.charAt(i);
            final byte b1 = (byte) c, b2 = (byte) (c >>> 8);
            assert ((b1 & 0xFF) | ((b2 & 0xFF) << 8)) == c; // no information loss
            data[i * 2] = b1;
            data[i * 2 + 1] = b2;
        }

        int offset = 0;
        int len = data.length;
        int seed = 0;
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        int h1 = seed;
        int roundedEnd = offset + (len & 0xfffffffc);  // round down to 4 byte block
        for (int i = offset; i < roundedEnd; i += 4) {
            // little endian load order
            int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8) | ((data[i + 2] & 0xff) << 16) | (data[i + 3] << 24);
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail
        int k1 = 0;
        switch (len & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
                // fallthrough
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
                // fallthrough
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= len;

        // fmix(h1);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return h1;
    }

    /**
     * 根据哈希值与主分片数量计算分片ID
     *
     * @param hash                  哈希值
     * @param numberOfPrimaryShards 主分片数量
     * @return 分片ID
     */
    public static int shardId(int hash, int numberOfPrimaryShards) {

        return Math.floorMod(hash, numberOfPrimaryShards);
    }

    public static String getMd5Value(String input) {
        return DigestUtils.md5Hex(input);
    }

    public static void main(String[] args) {
        String ss = "https://m.sogou.com/web/searchList.jsp?keyword=%E6%89%8B%E6%9C%BA%E5%85%85%E7%94%B5%E5%99%A8%E5%A4%9A%E5%B0%91%E7%93%A6%E6%98%AF%E5%BF%AB%E5%85%85&wm=3206";
        String md5Start16 = IDUtils.getMd5Value(ss).substring(0,16);
        System.out.println(md5Start16);
        //十进制，
        byte[] rk = Bytes.toBytes(String.format("%03d_%s", IDUtils.shardId(IDUtils.hash(md5Start16), 100), md5Start16));
        byte[] rk3 = Bytes.toBytes(String.format("%03d_%s_%s", IDUtils.shardId(IDUtils.hash(md5Start16), 100),"20191123", md5Start16));
        System.out.println(Bytes.toString(rk3));
        System.out.println(IDUtils.shardId(IDUtils.hash(md5Start16), 100));//分区
        System.out.println(Bytes.toString(rk));
        //十六进制，一般hbase设置成256*256个分区
        byte[] rk2 = Bytes.toBytes(String.format("%04x_%s", IDUtils.shardId(IDUtils.hash(md5Start16), 100), md5Start16));
        System.out.println(Bytes.toString(rk2));

    }
}



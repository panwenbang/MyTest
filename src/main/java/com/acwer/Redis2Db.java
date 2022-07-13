package com.acwer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 将库存一分为二，放入缓存当中,解决常见数据库库存与缓存不一致问题
 * 说明：本程序只是一个模拟应用程序
 * 模拟用到的技术：Redis、CAS锁、安全HASHMAP
 * 实际要用到的框架：Redis HASH、数据库(MYSQL、MSSQL)等,MQ(消息队列）
 */
public class Redis2Db {

    public static void main(String[] args) {

        Redis2Db db = new Redis2Db();
        //TODO:(简单模拟)随机放入一部分到缓存
        //实际使有ＭＱ时，应先尝试还原库存，再放入缓存
        for (Map.Entry<String, AtomicReference<商品库存>> entry : db.库存Table.entrySet()) {
            Random r = new Random();
            int cacheCount = 100+ r.nextInt(entry.getValue().get().库存 / 2);
            db.Redis2Count.put(entry.getKey(), new AtomicInteger(cacheCount));
            entry.getValue().get().库存 = entry.getValue().get().库存 - cacheCount;
            entry.getValue().get().冻结 = cacheCount;
        }

        db.列出商品列表();

        db.创建模拟订单();
        while (db.库存充足()) {
            new Thread(() -> {
               // sleep();
                db.创建模拟订单();
            }).start();

        }
        while (Thread.activeCount() > 2) {
            sleep();
        }

        db.列出商品列表();

    }

    static void sleep() {
        Random r = new Random();
        try {
            Thread.sleep(r.nextInt(100));
        } catch (Exception e) {
        }
    }

    //生成订单编号用到
    AtomicInteger orderGener = new AtomicInteger(1);

    public void 创建模拟订单() {
        int size = 库存Table.size();
        Random r = new Random();
        int loop = size + r.nextInt(size);
        Map<String, Integer> map = new HashMap<>();
        Enumeration<String> keys = 库存Table.keys();
        List<String> 商品名称s = new ArrayList<>();
        while (keys.hasMoreElements()) {
            商品名称s.add(keys.nextElement());
        }
        for (int i = 0; i < loop; i++) {
            String goodName = 商品名称s.get(r.nextInt(size));
            int count = 1 + r.nextInt(50);
            Integer orDefault = map.getOrDefault(goodName, 0);
            map.put(goodName, count + orDefault);
        }
        String orderNo = String.valueOf(Math.pow(10, 6) + orderGener.getAndIncrement());
        orderNo = orderNo.substring(1);
        下订单(map, orderNo);

    }

    public void 列出商品列表() {
        System.out.println("===商品库存====");
        for (Map.Entry<String, AtomicReference<商品库存>> entry : 库存Table.entrySet()) {
            String gn = entry.getKey();
            System.out.println(gn + "=>" + entry.getValue().get());
        }
    }

    public void 下订单(Map<String, Integer> goods, String orderNo) {
        System.out.println("===订单：" + orderNo + "=====");
        for (Map.Entry<String, Integer> entry : goods.entrySet()) {
            boolean success = 扣减库存(entry.getKey(), entry.getValue());
            if (success) {
                System.out.println(entry.getKey() + ":" + entry.getValue() + ",下单成功");
            } else {
                System.out.println(entry.getKey() + ":" + entry.getValue() + ",下单失败");
            }
        }
    }

    public void 还原库存(String good) {
        //实际使用时MQ消息
//        new Thread(()->{
            AtomicReference<商品库存> 商品库存Reference = 库存Table.get(good);
            //使用ＣＡＳ模拟数据库事务、原子
            for (; ; ) {
                商品库存 商品库存 = 商品库存Reference.get();
                if (商品库存.冻结 == 0) {
                    return;
                }
                if (商品库存.冻结.intValue() == 商品库存.消耗.intValue()) {
                    商品库存 n库存 = new 商品库存(商品库存.库存, 0, 0);
                    if (商品库存Reference.compareAndSet(商品库存, n库存)) {
                        break;
                    }
                } else break;
            }

//        }).start();
    }

    public void 累加消耗(String good, Integer count) {
        AtomicReference<商品库存> 商品库存Reference = 库存Table.get(good);
        for (; ; ) {
            商品库存 商品库存 = 商品库存Reference.get();
            商品库存 n库存 = new 商品库存(商品库存.库存, 商品库存.冻结, 商品库存.消耗 + count);
            if (商品库存Reference.compareAndSet(商品库存, n库存)) {
                break;
            }
        }
    }

    ConcurrentHashMap<String, AtomicInteger> Redis2Count = new ConcurrentHashMap<>();

    public boolean 扣减库存(String goodName, Integer count) {
        AtomicInteger cacheCounter = null;

        for (; (cacheCounter = Redis2Count.get(goodName)) != null; ) {
            Integer cacheCount = cacheCounter.get();
            if (cacheCount >= count) {
                //实际使用当中用Redis incr
                if (cacheCounter.compareAndSet(cacheCount, cacheCount - count)) {
                    //模拟MQ消息更新缓存消耗
//                    new Thread(()->{
                        累加消耗(goodName, count);
//                    }).start();
                    return true;
                }
            } else {
                //如果缓存当中的数量不足以扣减
                AtomicInteger remove = Redis2Count.remove(goodName);
                //将当前剩余的数量加到数据库并修改冻结列的值
                redis2db(goodName, remove.get());
                break;
            }
        }
        return db扣减库存(goodName, count);
    }

    /**
     * 将缓存当中的商品数量移入数据库库存
     *
     * @param good  商品名称
     * @param count 移入数量
     */
    private void redis2db(String good, Integer count) {
        AtomicReference<商品库存> 商品库存Reference = 库存Table.get(good);
        for (; ; ) {
            商品库存 库存 = 商品库存Reference.get();

            Integer 冻结数 = 库存.冻结 - count;

            商品库存 n库存 = new 商品库存(库存.库存 + count, 冻结数, 库存.消耗);
            if (商品库存Reference.compareAndSet(库存, n库存)) {
                break;
            }
        }
        还原库存(good);
    }

    /**
     * 数据库当中扣减库存
     *
     * @param goodName
     * @param count    扣减成功返回True,库存不足返回 false
     */
    private boolean db扣减库存(String goodName, Integer count) {

        AtomicReference<商品库存> 商品库存Reference = 库存Table.get(goodName);
        //原子更新库存
        for (; ; ) {
            商品库存 商品库存 = 商品库存Reference.get();
            if (商品库存.库存 >= count) {
                商品库存 n库存 = new 商品库存(商品库存.库存 - count, 商品库存.冻结, 商品库存.消耗);
                if (商品库存Reference.compareAndSet(商品库存, n库存)) {
                    return true;
                }
            } else break;
        }
        return false;

    }

    static class 商品库存 {
        public Integer 库存;
        public Integer 冻结;
        public Integer 消耗;

        public 商品库存(Integer a, Integer b, Integer c) {
            库存 = a;
            冻结 = b;
            消耗 = c;
        }


        public Integer 总库存() {
            int total = 库存 + 冻结;
            return total;
        }

        @Override
        public String toString() {
            return "库存=" + 库存 +
                    ", 冻结=" + 冻结 +
                    ", 消耗=" + 消耗;
        }
    }

    public boolean 库存充足() {
        return 库存Table.entrySet().stream().map(it -> {
            商品库存 商品库存 = it.getValue().get();
            return 商品库存.库存 + 商品库存.冻结 - 商品库存.消耗;
        }).filter(it -> it > 0).count() > 0;
    }

    //商品库存表
    ConcurrentHashMap<String, AtomicReference<商品库存>> 库存Table = new ConcurrentHashMap<String, AtomicReference<商品库存>>() {
        {
            put("牙刷", new AtomicReference<>(new 商品库存(1000, 0, 0)));
            put("手表", new AtomicReference<>(new 商品库存(2000, 0, 0)));
            put("手机", new AtomicReference<>(new 商品库存(3000, 0, 0)));
            put("袜子", new AtomicReference<>(new 商品库存(5000, 0, 0)));
            put("皮鞋", new AtomicReference<>(new 商品库存(10000, 0, 0)));
        }
    };

}

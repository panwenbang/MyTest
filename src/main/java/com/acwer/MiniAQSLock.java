package com.acwer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * ReentrantLock、AQS权威解析，看懂彻底明白ReentrantLock底层原理
 * １、代码清晰简洁，深入理解AQS锁
 * 2、不支持锁重入，只是让人理解底层原因
 * 3、全面掌握ＣＡＳ的使用
 */
public class MiniAQSLock {

    static int count=0;

    //TODO:测试
    public static void main(String[] args) {

        MiniAQSLock lock=new MiniAQSLock();

        List<Thread> threads=new ArrayList<>();

        //---------测试 开始--------------
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                //sleep模拟线程切换
                lock.lock();
                sleep();
                count++;
                lock.unlock();
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            }catch (Exception ex){}
        }
        System.out.println(count);

        //---------测试结束-----------------
    }
    static void sleep(){
        Random r=new Random();
        try{
            Thread.sleep(r.nextInt(10));
        }catch (Exception ex){}
    }


    private AtomicBoolean busy=new AtomicBoolean(false);


    public void lock(){
        //尝试获取锁
        if(!tryLock()){
            //获取锁失败就入队列
            queenHold(addWaiter(new Node(Thread.currentThread())));
        }
    }

    private boolean tryLock(){
        return busy.compareAndSet(false,true);
    }

    private boolean shouldPark(Node p){
        AtomicBoolean waitStatus = p.waitStatus;
        boolean b = waitStatus.get();
        //判断当前状态是不是需要唤醒后一个节点
        if(b){
            return true;
        }
        //如果不是改成true
        waitStatus.compareAndSet(b,true);
        return false;
    }

    private void unpark(Node p){
        AtomicBoolean waitStatus = p.waitStatus;
        boolean ws = waitStatus.get();
        if(ws){
            //这一步才是解锁的关键，如果没有这一步，你park住的线程将无法再被unpark
            waitStatus.compareAndSet(ws,false);
        }
        Node s = p.next;
        if(s!=null){
            LockSupport.unpark(s.thread);
        }
    }
    private void queenHold(Node node){
        for (;;){
            Node pre = node.pre;
            //开始：其实锁到这已经能够保证线程的安全啦,缺点就是这里会一直在循环尝试获取锁，占用了大量的ＣＰＵ时间
            if(pre==head&&tryLock()){
                pre.next=null;
                head=node;
                return;
            }
            //结束
            //LockSupport.park(this);

            //这一步不是必须，但有这一步是性能的关键,也才能使这个锁真正可以在生产环境中使用
            if(shouldPark(pre)){
                //park住线程，使其不一直占用ＣＰＵ时间片
                LockSupport.park(this);
            }
        }
    }

    public void unlock(){
        busy.set(false);
        if(head!=null&&head.waitStatus.get())
            unpark(head);
    }
    //添加Node到尾节点
    private Node addWaiter(Node node){
        for (;;){
            Node tail= holdTail.get();
            node.pre=tail;
            if(holdTail.compareAndSet(tail,node)){
                tail.next=node;
                break;
            }
        }
        return node;
    }
    private Node head=new Node();
    //使用AtomicReference 保护尾节点的线程安全
    private AtomicReference<Node> holdTail=new AtomicReference<>(head);

    /**
     * 队列ＮＯＤＥ
     */
    class Node{
        //线程
        public volatile Thread thread;
        //前一个节点
        public volatile Node pre;
        //后一个节点
        public volatile Node next;
        //如果是TRUE,代表需要唤醒后面的线程。如果False，代表不需要唤醒后面的线程
        //也可以理解为：前一个线程调用了unlock没有，如果已经调用过unlock,那么不能PARK，因为已经没有线程再唤醒park住的线程
        public AtomicBoolean waitStatus=new AtomicBoolean(false);

        public Node(){}
        public Node(Thread t){
            this.thread=t;
        }
    }
}
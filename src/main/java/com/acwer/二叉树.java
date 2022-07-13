package com.acwer;

import java.util.LinkedList;
import java.util.Queue;

public class 二叉树 {

    public static void main(String[] args) {

        CBTManager cbt=new CBTManager();
        for (int i = 1; i <= 20; i++) {
            cbt.Insert(i);
        }
        cbt.print();
        System.out.println("out");
    }

}

class CBTManager<T> {

    private TreeNode<T> root;
    Queue<TreeNode<T>> queue=new LinkedList<TreeNode<T>>();

    public void print(){
        Queue<TreeNode<T>> queue=new LinkedList<TreeNode<T>>();
        queue.offer(root);
        while (queue.peek()!=null){
            System.out.println(queue.peek());
            queue.offer(queue.peek().left);
            queue.offer(queue.peek().right);
            queue.poll();
        }
    }
    public void Insert(T data){
        TreeNode<T> tNode = new TreeNode<>(data);
        if(root==null){
            this.root=tNode;
            queue.offer(this.root);
            return;
        }
        TreeNode<T> peek = queue.peek();
        if(peek.left==null){
            peek.left=tNode;
        }else {
            peek.right=tNode;
            queue.offer(peek.left);
            queue.offer(peek.right);
            queue.poll();

        }
    }
}




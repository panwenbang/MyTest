package com.acwer.tree;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AVLTreeDemo{
    public static void main(String[] args) {
        //int []arr={10,12,8,9,7,6};

        HashMap map=new HashMap();
        map.put("","");

        int []arr=new int[10000];
        Random r=new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i]=r.nextInt(arr.length);
        }
        long ts=new Date().getTime();
        Node root=null;
        for (int i : arr) {
            if(root==null) {
                root=new Node(i);
                continue;
            }
            root.add(i);
        }
        System.out.println((new Date().getTime()-ts)*1.0/1000);
        System.out.println(root);
        System.out.println(root.leftHeight() + ":" + root.rightHeight());
    }

    static class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(){}
        public Node(int v){
            value=v;
        }

        public void add(int v){
            Node node=new Node(v);
            if(v<value){
                if(left==null) left=node;
                else left.add(v);
            }else {
                if(right==null) right=node;
                else right.add(v);
            }

            if(leftHeight()-rightHeight()>1){
                if(left!=null&&left.rightHeight()>left.leftHeight()){
                    left.leftRotate();
                }
                rightRotate();
                return;
            }
            if(rightHeight()-leftHeight()>1){
                if(right!=null&&right.leftHeight()>right.rightHeight()){
                    right.rightRotate();
                }
                leftRotate();
                return;
            }
        }
        //左旋
        public void leftRotate(){
            Node node=new Node(value);
            node.left=left;
            node.right=right.left;
            value=right.value;
            right=right.right;
            left=node;
        }
        //右旋
        public void rightRotate(){
            Node node=new Node(value);
            node.right=right;
            node.left=left.right;
            value=left.value;
            left=left.left;
            right=node;
        }
        public int leftHeight(){
            if(left==null) return 0;
            return left.height();
        }
        public int rightHeight(){
            if(right==null) return 0;
            return right.height();
        }
        public int height(){
            return Math.max(left==null?0: left.height(), right==null?0: right.height())+1;
        }
    }
}
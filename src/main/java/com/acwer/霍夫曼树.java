package com.acwer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class 霍夫曼树 {
    public static void main(String[] args) {
        int[] arr={13,7,8,3,29,6,1};
        霍夫曼树 huffMan=new 霍夫曼树();
        TreeNode<Integer> huffManTree = huffMan.createHuffManTree(arr);
        System.out.println("out");
    }

    public TreeNode<Integer> createHuffManTree(int [] arr){

        List<TreeNode<Integer>> nodes=new ArrayList<TreeNode<Integer>>();
        for (int i : arr) {
            nodes.add(new TreeNode<Integer>(i));
        }

        while (nodes.size()>1){
            //１、先对node从小到大排序
            Collections.sort(nodes,(a,b)->a.val-b.val);
            //2.取出第一个和第二个数组，组成一个TreeNode
            TreeNode<Integer> left = nodes.get(0);
            TreeNode<Integer> right = nodes.get(1);
            //3.组成一个新的TreeNode
            TreeNode<Integer> parent=new TreeNode<Integer>(left.val+right.val);
            parent.left=left;
            parent.right=right;
            nodes.remove(left);
            nodes.remove(right);
            //4.将新的节点加入到node,并重复１－４步，直到nodes.size==1
            nodes.add(parent);
        }

        return nodes.get(0);
    }
}

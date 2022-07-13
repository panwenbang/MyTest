package com.acwer;

public class TreeNode<T> {
    T val;
    TreeNode left;
    TreeNode right;

    TreeNode() {
    }

    TreeNode(T val) {
        this(val, null, null);
    }

    TreeNode(T val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "val=" + val +
                '}';
    }
}
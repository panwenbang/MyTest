package com.acwer;

import java.util.ArrayList;
import java.util.List;

class Solution {


    public static void main(String[] args) {
        Solution s=new Solution();
        System.out.println(s.subsets(new int[]{0,1,2}));
    }
    //结果集合
    List<List<Integer>> res = new ArrayList<>();
    //子集
    List<Integer> list = new ArrayList<>();
    //数组
    int[] nums;

    public List<List<Integer>> subsets(int[] nums) {
        this.nums = nums;
        dfs(0);
        return res;
    }


    /**
     * 深度优先+回溯
     * @param n
     */
    /**
     * 0.0 list=[] res=[[]]
     *     i=0 list[0]
     *     i=1 res=[[],[0]]
     * @param n
     */
    public void dfs(int n){
        //添加子集
        res.add(new ArrayList(list));
        //遍历
        for(int i = n; i < nums.length; ++i){
            //添加该元素
            list.add(nums[i]);
            //深度遍历
            dfs(i+1);
            //删除该元素
            list.remove(list.size()-1);
        }
    }
}
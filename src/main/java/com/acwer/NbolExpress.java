package com.acwer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NbolExpress {

    public static void main(String[] args) {
        String str="1+((2+3)*4)-5";
        List<String> 操作列表 = decode(字符转Ls(str));
        System.out.println(String.join(",", 操作列表));
        System.out.println(计算(操作列表));
    }

    public static double 计算(List<String> 逆波兰表达式){

        Map<String, Integer> 运算符优先级 = 获取运算符优先级();
        double sum=0;
        Stack<Double> stack=new Stack<Double>();

        for (String s : 逆波兰表达式) {
            if(运算符优先级.containsKey(s)){
                switch (s){
                    case "+":
                        stack.push(stack.pop()+stack.pop());
                        break;
                    case "-":
                        {
                            Double a=stack.pop();
                            Double b=stack.pop();
                            stack.push(b-a);
                        }
                        break;
                    case "*":
                        stack.push(stack.pop()*stack.pop());
                        break;
                    case "/":
                        {
                            Double a=stack.pop();
                            Double b=stack.pop();
                            stack.push(b/a);
                        }
                        break;
                }
            }else {
                stack.push(Double.valueOf(s));
            }
        }
        sum=stack.pop();
        return sum;

    }

    public static Map<String,Integer> 获取运算符优先级(){
        return new HashMap<String,Integer>(){
            {
                put("+",1);
                put("-",1);
                put("*",2);
                put("/",2);
                put("(",0);
                put(")",0);
            }
        };
    }

    public static List<String> decode(List<String> 操作列表){

        Stack<String> 操作符=new Stack<>();
        Stack<String> 运算符=new Stack<>();

        Map<String,Integer> 优先级=获取运算符优先级();
        String 符号="+-*/()";
        for (String s : 操作列表) {
            boolean 是运算符 = 符号.contains(s);
            if(!是运算符) 操作符.add(s);
            else {
                //运算符逻辑开始
                //1.如果是左括号或运算符栈为空，直接入栈
                if(s.equals("(")||运算符.isEmpty()){
                    运算符.push(s);
                }
                //2.如果是右括号
                else if(s.equals(")")){
                    while (!运算符.isEmpty()&&!运算符.peek().equals("(")){
                        操作符.add(运算符.pop());
                    }
                    运算符.pop();
                }
                //3.优先级小于等于当前栈顶元素
                else if(优先级.get(s)<=优先级.get(运算符.peek())){
                   do {
                       操作符.add(运算符.pop());
                   }while (!运算符.isEmpty()&&优先级.get(s)<=优先级.get(运算符.peek()));
                   运算符.add(s);
                }
                //4.优先级大于当前栈顶元素
                else if(优先级.get(s)>优先级.get(运算符.peek())){
                    运算符.add(s);
                }
            }

        }
        //将所有栈里面的元素加入操作符
        while (!运算符.isEmpty()){
            操作符.add(运算符.pop());
        }

        return 操作符;
    }
    //字符拆分
    public static List<String> 字符转Ls(String str){
        //拆分符号
        //String 符号="+-*/()";
        Pattern compile = Pattern.compile("(\\d(\\.*))+");
        Matcher matcher = compile.matcher(str);
        List<String> ls=new ArrayList<>();
        int preEnd=-1;
        while (matcher.find()){
            String group = matcher.group(0);
            if(preEnd==-1){
                preEnd=matcher.end();
                ls.add(group);
                continue;
            }
            int start = matcher.start();

            for (int i = preEnd; i < start; i++) {
                String 符号 = str.substring(i, i + 1);
                ls.add(符号);
            }
            ls.add(group);

            preEnd= matcher.end();
        }
        return ls;
    }

}

package me.asu.security.util;

public class Wildcard {
    // 字符串匹配支持通配符 ? *,简单算法
    public static boolean matches(String pattern, String input) {
        if(isEmpty(input)) return false;
        if ("*".equals(pattern))  return true;
        char[] p = pattern.toCharArray();
        int n = p.length;
        char[] s = input.toCharArray();
        int i =0,j=0,iStart=-1,jStart=-1, k = s.length;
        while(i<k) {
            if (j<n && (s[i] == p[j] || p[j] == '?')) {
                i++;
                j++;
            } else if (j < n && p[j] == '*') {
                // 记录如果之后序列匹配不成功时，i, j 需要回溯到的位置
                iStart = i; // 记录星号位置
                jStart = j++; // 记录星号位置， 并且j移到下一位，准备下个循环
            } else if (iStart >= 0) {
                // 发现字符不匹配，且没有星号出现，但是iStart > 0,
                // 说明可能是*匹配的字符数量不对，这时回溯
                // i 回溯到iStart + 1 因为上次从s串iStart开始对*尝试匹配已经证明
                // 是不成功的，所以从标记的下一个位置开始再试，同时更新标记位置
                i = ++iStart;
                // 重置 p 位置
                j = jStart + 1;
            } else {
                return false;
            }
        }
// 删除多余的星号
        while(j<n && p[j] == '*') ++j;
        return j == n;
    }

    private static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }
}

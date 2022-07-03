package com.zpw.myplayground;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExampleUnitTest {

    @Test
    public void test() {
        List<List<String>> results = partition("abcba");
        System.out.println(results);
    }

    public List<List<String>> partition(String s) {
        List<List<String>> results = new ArrayList<>();
        backtrack(results, new ArrayList<>(), s, 0);
        return results;
    }

    public void backtrack(List<List<String>> results, List<String> tempList, String s, int startIndex) {
        if (startIndex == s.length()) {
            results.add(new ArrayList<>(tempList));
            return;
        }
        // 第一轮遍历的都是单个字母：a,b,c,b,a
        // 第二轮遍历的是：ba, cb,cba, bc,bcb,a,bcba
        // 第三轮遍历的是：ab,abc,abcb,abcba
        for(int i = 1; i <= s.length() - startIndex; i++) {
            int endIndex = startIndex + i;
            String subString = s.substring(startIndex, endIndex);
            if(isPalindrome(subString)) {
                tempList.add(subString);
                backtrack(results, tempList, s, endIndex);
                tempList.remove(tempList.size() - 1);
            }
        }
    }

    public boolean isPalindrome(String s) {
        if (s.length() == 1) return true;
        char[] chars = s.toCharArray();
        int low = 0;
        int high = chars.length - 1;
        while (low < high) {
            if(chars[low] != chars[high]) return false;
            low++;
            high--;
        }
        return true;
    }

    public int[] findDuplicates(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            while (nums[i] != nums[nums[i] - 1]) {
                swap(nums, i, nums[i] - 1);
            }
        }
        return nums;
    }

    public void swap(int[] nums, int index1, int index2) {
        int temp = nums[index1];
        nums[index1] = nums[index2];
        nums[index2] = temp;
    }
}
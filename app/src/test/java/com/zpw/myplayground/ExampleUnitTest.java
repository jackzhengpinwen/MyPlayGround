package com.zpw.myplayground;

import org.junit.Test;

public class ExampleUnitTest {

    @Test
    public void test() {
        findDuplicates(new int[]{4,3,2,7,8,2,3,1});
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
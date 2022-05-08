package com.zpw.myplayground.leetcode

import java.util.*

/**
 * 数组中重复的数据
 *
 * 给你一个长度为 n 的整数数组 nums ，其中 nums 的所有整数都在范围 [1, n] 内，且每个整数出现 一次 或 两次 。
 * 请你找出所有出现 两次 的整数，并以数组形式返回。
 *
 * 方法：将元素交换到【元素 - 1】的位置
 */
fun findDuplicates(nums: IntArray): MutableList<Int> {
    val n = nums.size
    for (i in 0 until n) {
        // 如果数组中的某个数字已经出现在下标(数字 - 1)的位置，那么就不交换，如果某个数字并没有出现在下标(数字 - 1)的位置，那么就交换
        while (nums[i] != nums[nums[i] - 1]) {
            swap(nums, i, nums[i] - 1)
        }
    }
    val ans = mutableListOf<Int>()
    for (i in 0 until n) {
        // 因为所有元素都交换到【元素 - 1】的位置了，所以如果元素值与下标不相同，那么就是多余的数
        if(nums[i] - 1 != i) {
            ans.add(nums[i])
        }
    }
    return ans
}

/**
 * 盛最多水的容器
 *
 * 给定一个长度为 n 的整数数组height。有n条垂线，第 i 条线的两个端点是(i, 0)和(i, height[i])。
 * 找出其中的两条线，使得它们与x轴共同构成的容器可以容纳最多的水。返回容器可以储存的最大水量。
 *
 * 方法：双指针
 */
fun maxArea(height: IntArray): Int {
    var left = 0
    var right = height.size - 1
    var ans = 0
    while (left < right) {
        val area = Math.min(height[left], height[right]) * (right - left)
        ans = Math.max(ans, area)
        // height[left] 小于 height[right] 时，再怎么左移 right 也没办法增加 area
        if (height[left] < height[right]) {
            left++
        } else {
            right--
        }
    }
    return ans
}

/**
 * 三数之和
 *
 * 给你一个包含 n 个整数的数组nums，判断nums中是否存在三个元素 a，b，c ，使得a + b + c = 0 ？请你找出所有和为 0 且不重复的三元组。
 * 注意：答案中不可以包含重复的三元组。
 *
 * 方法：排序去重 + 将问题转换为twosum，求解某个数的负数
 */
fun threeSum(nums: IntArray): List<List<Int>> {
    val n = nums.size
    Arrays.sort(nums)
    val ans = mutableListOf<List<Int>>()
    for (first in 0 until n) {
        // 根据排序后的结果去重
        if (first > 0 && nums[first] == nums[first - 1]) continue
        var third = n - 1
        // 将求三数和转换为求两数和等于他们的负数
        var target = -nums[first]
        for (second in first + 1 until n) {
            // 根据排序后的结果去重
            if (second > first + 1 && nums[second] == nums[second - 1]) continue
            // 需要保证 b 的指针在 c 的指针的左侧
            while (second < third && nums[second] + nums[third] > target) third--
            // 如果指针重合，随着 b 后续的增加
            // 就不会有满足 a+b+c=0 并且 b<c 的 c 了，可以退出循环
            if (second == third) break;
            if (nums[second] + nums[third] == target) {
                val list = mutableListOf<Int>()
                list.add(nums[first])
                list.add(nums[second])
                list.add(nums[third])
                ans.add(list)
            }
        }
    }
    return ans
}

fun swap(nums: IntArray, i: Int, j: Int) {
    val tmp = nums[i]
    nums[i] = nums[j]
    nums[j] = tmp
}
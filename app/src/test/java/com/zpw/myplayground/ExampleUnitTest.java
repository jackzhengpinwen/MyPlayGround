package com.zpw.myplayground;

import org.junit.Test;

public class ExampleUnitTest {

    @Test
    public void test() {
        String s = "bbbcccdddaaa";
        int[] widths = new int[] {4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
        int[] ans = numberOfLines(widths, s);
        System.out.println(ans[0]);
        System.out.println(ans[1]);
    }

    public int[] numberOfLines(int[] widths, String s) {
        int count = 0;
        for(int i = 0; i < s.length(); i++) {
            count += widths[s.charAt(i) - 'a'];
            System.out.println(count);
        }
        int op = count % 100;
        int row = count / 100 + (op > 0 ? 1 : 0);
        return new int[] {row, op};
    }
}
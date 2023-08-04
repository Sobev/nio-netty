package org.sobev.io_test.zerowithchar;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luojx
 * @date 2022/7/16 9:32
 */
public class ZeroWithUtil {

    public static void main(String[] args) {
        String msg = "howudoing!!";
        String zeroWith = strToZeroWidth(msg);
        System.out.println("zeroWith = " + zeroWith);
        System.out.println(zeroWidthToStr(zeroWith));
    }

    public static String strToZeroWidth(String str){
        List<String> list = Arrays.stream(str.split(""))
                .map(ch -> {
                    return Integer.toUnsignedString(ch.codePointAt(0), 2);
                }).collect(Collectors.toList());

        for (String s : list) {
            System.out.println("s = " + s);
        }

        String reduce = list.stream().map(item -> {
            StringBuilder builder = new StringBuilder();
            char[] chars = item.toCharArray();
            for (char c : chars) {
                if (c == '1') {
//                    builder.append('‍');
                    builder.append("‍");
                } else {
//                    builder.append('‌');
                    builder.append("‌");
                }
            }
            return builder.toString();
        }).reduce("", (cur, iter) -> {
//            常见的空格 '​'
            return cur + "​" + iter;
        });
        return reduce;
    }
    
    
    public static String zeroWidthToStr(String zeroStr){
        String[] split = zeroStr.split("​");
        for (String s : split) {
            System.out.println("s = " + s);
        }
        return Arrays.stream(split)
                .map(item -> {
                    StringBuilder builder = new StringBuilder();
                    char[] chars = item.toCharArray();
                    for (char c : chars) {
                        if(c == '\u200d'){
                            builder.append('1');
                        }else if(c == '\u200c'){
                            builder.append('0');
                        }else{
                            builder.append(" ");
                        }
                    }
                    System.out.println("builder = " + builder);
                    int codePoint = Integer.parseUnsignedInt(builder.toString(), 2);
                    return String.valueOf(codePoint);
                }).collect(Collectors.joining());
    }
}

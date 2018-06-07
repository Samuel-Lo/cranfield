package edu.kennesaw.cs.core;
import edu.kennesaw.cs.readers.Query;

import java.util.ArrayList;
import java.util.List;

public class Normalization {

//    static char[] removeArr = {
//            '`', '!', '@', '_',
//            '#', '$', '%', '.',
//            '^', '&', '*', '-'
//    };

    static char[] removeArr = {
            '_', '.', '-', '/', '(', ')', '\'', '*', '$', '+', '=', ',', '\\'
    };

    public static void main(String[] args)
    {
        String normalized_string = "asdue//";
        if(!normalized_string.equals("the")){
            System.out.println(true);
        }
        List<String> test = new ArrayList<String>();
        combine("abcde", new StringBuffer(), 0, test);
        for (int i = 0; i < test.size(); i++){
            System.out.println(test.get(i));
        }
        normalized_string = normalization(normalized_string);
        System.out.println(normalized_string);
    }

    public static String normalization(String s){
        for (int i = 0; i < removeArr.length; i++){
            s = removeChar(s, removeArr[i]);
        }
        return s;
    }

    public static String removeChar(String s, char c) {
        List<String> x = new ArrayList<String>();
        StringBuffer r = new StringBuffer(s.length());
        int count = 0;
        r.setLength(s.length());
        int current = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != c) {
                r.setCharAt(current++, ch);
                count++;
            }
//            else if(ch == '-'){
//                r.setCharAt( current++, ' ');
//                count++;
//            }
        }
        current = 0;
        String rch = r.toString();

        StringBuffer normalization = new StringBuffer(count);
        normalization.setLength(count);
        for (int i = 0; i < count; i++) {
            char ch = rch.charAt(i);
            normalization.setCharAt(current++, ch);
            x.add(normalization.toString());
        }

        return normalization.toString();
    }

    public static void combine(String instr, StringBuffer outstr, int index, List<String> outputList)
    {
        for (int i = index; i < instr.length(); i++)
        {
            outstr.append(instr.charAt(i));
            if (outstr.length()>1)
                outputList.add(outstr.toString());
            combine(instr, outstr, i + 1, outputList);
            //outstr.deleteCharAt(outstr.length() - 1);
            break;
        }
    }
}

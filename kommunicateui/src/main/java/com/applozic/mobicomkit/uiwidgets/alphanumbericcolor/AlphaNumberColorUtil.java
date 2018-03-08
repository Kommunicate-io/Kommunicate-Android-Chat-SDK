package com.applozic.mobicomkit.uiwidgets.alphanumbericcolor;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devashish on 23/1/15.
 */
public class AlphaNumberColorUtil {

    public static Map<Character, Integer> alphabetBackgroundColorMap = new HashMap<Character, Integer>();
    public static Map<Character, Integer> alphabetTextColorMap = new HashMap<Character, Integer>();

    public static Map<Integer, Integer> randomAlphabetBackgroundColorMap = new HashMap<Integer, Integer>();

    static {
        randomAlphabetBackgroundColorMap.put(0, R.color.mg_alphabet_first);
        randomAlphabetBackgroundColorMap.put(1, R.color.mg_alphabet_second);
        randomAlphabetBackgroundColorMap.put(2, R.color.mg_alphabet_third);
        randomAlphabetBackgroundColorMap.put(3, R.color.mg_alphabet_fourth);
        randomAlphabetBackgroundColorMap.put(4, R.color.mg_alphabet_fifth);
        randomAlphabetBackgroundColorMap.put(5, R.color.mg_alphabet_sixth);

        alphabetBackgroundColorMap.put(null, R.color.non_alpha);
        alphabetBackgroundColorMap.put('A', R.color.alphabet_a);
        alphabetBackgroundColorMap.put('B', R.color.alphabet_b);
        alphabetBackgroundColorMap.put('C', R.color.alphabet_c);
        alphabetBackgroundColorMap.put('D', R.color.alphabet_d);
        alphabetBackgroundColorMap.put('E', R.color.alphabet_e);
        alphabetBackgroundColorMap.put('F', R.color.alphabet_f);
        alphabetBackgroundColorMap.put('G', R.color.alphabet_g);
        alphabetBackgroundColorMap.put('H', R.color.alphabet_h);
        alphabetBackgroundColorMap.put('I', R.color.alphabet_i);
        alphabetBackgroundColorMap.put('J', R.color.alphabet_j);
        alphabetBackgroundColorMap.put('K', R.color.alphabet_k);
        alphabetBackgroundColorMap.put('L', R.color.alphabet_l);
        alphabetBackgroundColorMap.put('M', R.color.alphabet_m);
        alphabetBackgroundColorMap.put('N', R.color.alphabet_n);
        alphabetBackgroundColorMap.put('O', R.color.alphabet_o);
        alphabetBackgroundColorMap.put('P', R.color.alphabet_p);
        alphabetBackgroundColorMap.put('Q', R.color.alphabet_q);
        alphabetBackgroundColorMap.put('R', R.color.alphabet_r);
        alphabetBackgroundColorMap.put('S', R.color.alphabet_s);
        alphabetBackgroundColorMap.put('T', R.color.alphabet_t);
        alphabetBackgroundColorMap.put('U', R.color.alphabet_u);
        alphabetBackgroundColorMap.put('V', R.color.alphabet_v);
        alphabetBackgroundColorMap.put('W', R.color.alphabet_w);
        alphabetBackgroundColorMap.put('X', R.color.alphabet_x);
        alphabetBackgroundColorMap.put('Y', R.color.alphabet_y);
        alphabetBackgroundColorMap.put('Z', R.color.alphabet_z);


        alphabetTextColorMap.put(null, R.color.text_non_alpha);
        alphabetTextColorMap.put('A', R.color.alphabet_text_a);
        alphabetTextColorMap.put('B', R.color.alphabet_text_b);
        alphabetTextColorMap.put('C', R.color.alphabet_text_c);
        alphabetTextColorMap.put('D', R.color.alphabet_text_d);
        alphabetTextColorMap.put('E', R.color.alphabet_text_e);
        alphabetTextColorMap.put('F', R.color.alphabet_text_f);
        alphabetTextColorMap.put('G', R.color.alphabet_text_g);
        alphabetTextColorMap.put('H', R.color.alphabet_text_h);
        alphabetTextColorMap.put('I', R.color.alphabet_text_i);
        alphabetTextColorMap.put('J', R.color.alphabet_text_j);
        alphabetTextColorMap.put('K', R.color.alphabet_text_k);
        alphabetTextColorMap.put('L', R.color.alphabet_text_l);
        alphabetTextColorMap.put('M', R.color.alphabet_text_m);
        alphabetTextColorMap.put('N', R.color.alphabet_text_n);
        alphabetTextColorMap.put('O', R.color.alphabet_text_o);
        alphabetTextColorMap.put('P', R.color.alphabet_text_p);
        alphabetTextColorMap.put('Q', R.color.alphabet_text_q);
        alphabetTextColorMap.put('R', R.color.alphabet_text_r);
        alphabetTextColorMap.put('S', R.color.alphabet_text_s);
        alphabetTextColorMap.put('T', R.color.alphabet_text_t);
        alphabetTextColorMap.put('U', R.color.alphabet_text_u);
        alphabetTextColorMap.put('V', R.color.alphabet_text_v);
        alphabetTextColorMap.put('W', R.color.alphabet_text_w);
        alphabetTextColorMap.put('X', R.color.alphabet_text_x);
        alphabetTextColorMap.put('Y', R.color.alphabet_text_y);
        alphabetTextColorMap.put('Z', R.color.alphabet_text_z);
    }

}

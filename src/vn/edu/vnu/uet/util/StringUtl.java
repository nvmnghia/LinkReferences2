package vn.edu.vnu.uet.util;

public class StringUtl {
    /**
     * Clean the input string
     * - Trim
     * - Convert to lower case
     * - Strip diacritics
     *
     * @param str input string
     * @return cleaned string
     */
    public static String clean(String str) {
        return str.trim().toLowerCase();
    }

    public static String[] vietnameseChars = {
            "ô", "ơ"     , "â", "ă", "ê",           "ư",
            "ó", "ồ", "ớ", "á", "ấ", "ắ", "ề", "é", "ụ", "ứ", "í", "ý",
            "đ", "ò", "ộ", "ờ", "à", "ầ", "ằ", "ế", "è", "ú", "ừ", "ì", "ỳ",
            "õ", "ổ", "ở", "ã", "ẫ", "ẵ", "ễ", "ẽ", "ũ", "ữ", "ĩ", "ỷ",
            "ỏ", "ỗ", "ợ", "ả", "ẩ", "ẳ", "ể", "ẻ", "ủ", "ử", "ỉ", "ỹ",
            "ọ", "ố", "ỡ", "ạ", "ậ", "ặ", "ệ", "ẹ", "ù", "ự", "ị", "ỵ"};

    public static String[] normalizedVietnameseChars = {
            "o", "o"     , "a", "a", "e",           "u",
            "o", "o", "o", "a", "a", "a", "e", "e", "", "u", "i", "y",
            "d", "o", "o", "o", "a", "a", "a", "e", "e", "u", "u", "i", "y",
            "o", "o", "o", "a", "a", "a", "e", "e", "u", "u", "i", "y",
            "o", "o", "o", "a", "a", "a", "e", "e", "u", "u", "i", "y",
            "o", "o", "o", "a", "a", "a", "e", "e", "u", "u", "i", "y"};


    public static boolean isVietnamese(String str) {
        for (String vn : vietnameseChars) {
            if (str.contains(vn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all diacritics, accents,...
     * NFD is unnecessarily slow
     *
     * @param str
     * @return
     */
    public static String removeDiacritics(String str) {
        for (int i = 0; i < vietnameseChars.length; ++i) {
            str = str.replace(vietnameseChars[i], normalizedVietnameseChars[i]);
        }

        return str;
    }

    /**
     * Count the number of occurrences of a single letter in a string
     * Used in bitap
     *
     * @param haystack
     * @param needle
     * @return number of needle in the hackstack
     */
    public static int countChar(String haystack, char needle) {
        int counter = 0;

        for (int i = 0; i < haystack.length(); ++i) {
            if (haystack.charAt(i) == needle) {
                ++counter;
            }
        }

        return counter;
    }

    /**
     * Clean control chars before manually append strings to a JSON
     * @param string
     * @return
     */
    public static String cleanJSON(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':

                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;

                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;

                case '\b':
                    sb.append("\\b");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\f':
                    sb.append("\\f");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');

        return sb.toString();
    }
}

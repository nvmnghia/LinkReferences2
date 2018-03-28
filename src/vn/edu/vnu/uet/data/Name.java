package vn.edu.vnu.uet.data;

import vn.edu.vnu.uet.util.StringUtl;

import java.util.ArrayList;

public class Name {
    public static ArrayList<String> generateAbbrNames(String fullName) {
        String[] words = fullName.split(" ");

        // If the full name was abbreviated already, return null
        // No shortening can be done
        if (StringUtl.countChar(fullName, '.') >= words.length - 1) {
            return null;
        }

        // Else:
        ArrayList<String> abbrNames = new ArrayList<>();

        // Example Nguyen Viet Minh Nghia

        // Rule 1: nghia nguyen viet minh
        StringBuilder temp = new StringBuilder(words[words.length - 1]);
        for (int i = 0; i < words.length - 1; ++i) {
            temp.append(' ').append(words[i]);
        }
        abbrNames.add(temp.toString());

        // Rule 1: n. v. m. nghia
        temp = new StringBuilder("");
        for (int i = 0; i < words.length - 1; ++i) {
            temp.append(words[i].charAt(0)).append(". ");
        }
        temp.append(words[words.length - 1]);
        abbrNames.add(temp.toString());

        // Rule 2: nghia, n. v. m.
        temp = new StringBuilder(words[words.length - 1]).append(' ');
        for (int i = 0; i < words.length - 1; ++i) {
            temp.append(words[i].charAt(0)).append(". ");
        }
        abbrNames.add(temp.toString().trim());

        // Rule 3: nghia, n.
        temp = new StringBuilder(words[words.length - 1]).append(", ").append(words[0].charAt(0)).append('.');
        abbrNames.add(temp.toString());

        // Rule 4: n. nghia
        temp = new StringBuilder().append(words[0].charAt(0)).append(". ").append(words[words.length - 1]);
        abbrNames.add(temp.toString());

        return abbrNames;
    }
}

// Phased with Predefined Manglings

import java.io.*;
import java.util.*;

public class PasswordCrack {
    static class UserInfo {
        String username;
        String encryptedPassword;
        String salt;
        List<String> gcosWords;

        UserInfo(String line) {
            String[] parts = line.split(":");
            username = parts[0];
            encryptedPassword = parts[1];
            salt = encryptedPassword.length() >= 2 ? encryptedPassword.substring(0, 2) : "";
            gcosWords = new ArrayList<>();
            if (parts.length > 4) {
                for (String token : parts[4].split(" ")) {
                    if (!token.trim().isEmpty()) {
                        gcosWords.add(token.trim());
                    }
                }
            }
        }
    }

    static final int MAX_PASSWORD_LENGTH = 8;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java PasswordCrack <dictionary> <passwd>");
            return;
        }

        List<UserInfo> users = readUsers(args[1]);
        Map<String, UserInfo> passwordMap = new HashMap<>();
        for (UserInfo user : users) {
            passwordMap.put(user.encryptedPassword, user);
        }

        // Phase 1: Try GCOS words
        Iterator<UserInfo> iter = users.iterator();
        while (iter.hasNext()) {
            UserInfo user = iter.next();
            for (String word : user.gcosWords) {
                if (tryCrackAllManglings(word, user)) {
                    iter.remove();
                    break;
                }
            }
        }

        // Phase 2: Try dictionary words with basic mangling
        BufferedReader dictReader = new BufferedReader(new FileReader(args[0]));
        String word;
        while ((word = dictReader.readLine()) != null) {
            word = word.trim();
            if (word.isEmpty()) continue;
            if (word.length() > MAX_PASSWORD_LENGTH)
                word = word.substring(0, MAX_PASSWORD_LENGTH);

            Iterator<UserInfo> iterator = users.iterator();
            while (iterator.hasNext()) {
                UserInfo user = iterator.next();
                if (tryCrackAllManglings(word, user)) {
                    iterator.remove();
                }
            }
            if (users.isEmpty()) break;
        }
        dictReader.close();

        // Phase 3: Try dictionary words with additional mangling
        dictReader = new BufferedReader(new FileReader(args[0]));
        while ((word = dictReader.readLine()) != null && !users.isEmpty()) {
            word = word.trim();
            if (word.isEmpty()) continue;
            if (word.length() > MAX_PASSWORD_LENGTH)
                word = word.substring(0, MAX_PASSWORD_LENGTH);

            Iterator<UserInfo> iterator = users.iterator();
            while (iterator.hasNext()) {
                UserInfo user = iterator.next();
                if (tryCrackExtraManglings(word, user)) {
                    iterator.remove();
                }
            }
        }
        dictReader.close();
    }

    static List<UserInfo> readUsers(String passwdFile) throws IOException {
        List<UserInfo> users = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(passwdFile));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.contains(":")) continue;
            users.add(new UserInfo(line));
        }
        br.close();
        return users;
    }

    static boolean tryCrack(String guess, UserInfo user) {
        if (guess.length() > MAX_PASSWORD_LENGTH) return false;
        String encrypted = jcrypt.crypt(user.salt, guess);
        if (encrypted.equals(user.encryptedPassword)) {
            System.out.println(guess);
            return true;
        }
        return false;
    }

    static boolean tryCrackAllManglings(String word, UserInfo user) {
        for (String guess : generateManglings(word)) {
            if (tryCrack(guess, user)) {
                return true;
            }
        }
        return false;
    }

    static List<String> generateManglings(String word) {
        List<String> mangles = new ArrayList<>();
        mangles.add(word);
        mangles.add(word.toUpperCase());
        mangles.add(word.toLowerCase());
        mangles.add(capitalize(word));
        mangles.add(ncapitalize(word));
        mangles.add(toggleCase(word));
        mangles.add(new StringBuilder(word).reverse().toString());
        if (word.length() > 1) mangles.add(word.substring(1));
        if (word.length() > 1) mangles.add(word.substring(0, word.length() - 1));
        mangles.add("!" + word);
        mangles.add(word + "!");
        mangles.add("123" + word);
        mangles.add(word + "123");
        return mangles;
    }

    static boolean tryCrackExtraManglings(String word, UserInfo user) {
        for (String guess : generateExtraManglings(word)) {
            if (tryCrack(guess, user)) {
                return true;
            }
        }
        return false;
    }

    static List<String> generateExtraManglings(String word) {
        List<String> mangles = new ArrayList<>();
        // Prepend and append digits
        for (char c = '0'; c <= '9'; c++) {
            mangles.add(c + word);
            mangles.add(word + c);
        }
        // Duplicate the string
        mangles.add(word + word);
        // Reflect the string
        mangles.add(word + new StringBuilder(word).reverse().toString());
        mangles.add(new StringBuilder(word).reverse().toString() + word);
        return mangles;
    }

    static String capitalize(String word) {
        if (word.length() == 0) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    static String ncapitalize(String word) {
        if (word.length() == 0) return word;
        return Character.toLowerCase(word.charAt(0)) + word.substring(1).toUpperCase();
    }

    static String toggleCase(String word) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : word.toCharArray()) {
            sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            upper = !upper;
        }
        return sb.toString();
    }
}

//------------------------------------------------------------------------------------------------------------------------------------------------------

// Breadth-First, Layered Manglings

//import java.io.*;
//import java.util.*;
//
//public class PasswordCrack {
//    static class UserInfo {
//        String username;
//        String encryptedPassword;
//        String salt;
//        List<String> gcosWords;
//
//        UserInfo(String line) {
//            String[] parts = line.split(":");
//            username = parts[0];
//            encryptedPassword = parts[1];
//            salt = encryptedPassword.length() >= 2 ? encryptedPassword.substring(0, 2) : "";
//            gcosWords = new ArrayList<>();
//            if (parts.length > 4) {
//                for (String token : parts[4].split(" ")) {
//                    if (!token.trim().isEmpty()) {
//                        gcosWords.add(token.trim());
//                    }
//                }
//            }
//        }
//    }
//
//    static final int MAX_PASSWORD_LENGTH = 8;
//
//    public static void main(String[] args) throws Exception {
//        if (args.length != 2) {
//            System.err.println("Usage: java PasswordCrack <dictionary> <passwd>");
//            return;
//        }
//
//        List<UserInfo> users = readUsers(args[1]);
//        if (users.isEmpty()) return;
//
//        // Phase 1: Try GCOS words
//        Iterator<UserInfo> iter = users.iterator();
//        while (iter.hasNext()) {
//            UserInfo user = iter.next();
//            for (String word : user.gcosWords) {
//                if (tryCrack(word, user)) {
//                    iter.remove();
//                    break;
//                }
//            }
//        }
//
//        // Phase 2: Try dictionary words in breadth-first mangling layers
//        BufferedReader dictReader = new BufferedReader(new FileReader(args[0]));
//        String word;
//        while ((word = dictReader.readLine()) != null && !users.isEmpty()) {
//            word = word.trim();
//            if (word.isEmpty()) continue;
//            if (word.length() > MAX_PASSWORD_LENGTH)
//                word = word.substring(0, MAX_PASSWORD_LENGTH);
//
//            // Layer 0: Try original word
//            crackWordAtLevel(Collections.singletonList(word), users);
//            if (users.isEmpty()) break;
//
//            // Layer 1: Apply one mangling rule
//            List<String> level1 = applySingleManglings(word);
//            crackWordAtLevel(level1, users);
//            if (users.isEmpty()) break;
//
//            // Layer 2: Apply two mangling rules
//            List<String> level2 = new ArrayList<>();
//            for (String w1 : level1) {
//                level2.addAll(applySingleManglings(w1));
//            }
//            crackWordAtLevel(level2, users);
//        }
//        dictReader.close();
//    }
//
//    static List<UserInfo> readUsers(String passwdFile) throws IOException {
//        List<UserInfo> users = new ArrayList<>();
//        BufferedReader br = new BufferedReader(new FileReader(passwdFile));
//        String line;
//        while ((line = br.readLine()) != null) {
//            if (!line.contains(":")) continue;
//            users.add(new UserInfo(line));
//        }
//        br.close();
//        return users;
//    }
//
//    static void crackWordAtLevel(List<String> guesses, List<UserInfo> users) {
//        Iterator<UserInfo> iter = users.iterator();
//        while (iter.hasNext()) {
//            UserInfo user = iter.next();
//            for (String guess : guesses) {
//                if (tryCrack(guess, user)) {
//                    iter.remove();
//                    break;
//                }
//            }
//        }
//    }
//
//    static boolean tryCrack(String guess, UserInfo user) {
//        if (guess.length() > MAX_PASSWORD_LENGTH) return false;
//        String encrypted = jcrypt.crypt(user.salt, guess);
//        if (encrypted.equals(user.encryptedPassword)) {
//            System.out.println(guess);
//            return true;
//        }
//        return false;
//    }
//
//    static List<String> applySingleManglings(String word) {
//        List<String> mangles = new ArrayList<>();
//        String reversed = new StringBuilder(word).reverse().toString();
//
//        mangles.add(word.toUpperCase());
//        mangles.add(word.toLowerCase());
//        mangles.add(capitalize(word));
//        mangles.add(ncapitalize(word));
//        mangles.add(toggleCase(word));
//
//        if (word.length() > 1) {
//            mangles.add(word.substring(1));
//            mangles.add(word.substring(0, word.length() - 1));
//        }
//
//        for (char c = '0'; c <= '9'; c++) {
//            mangles.add(c + word);
//            mangles.add(word + c);
//        }
//
//        mangles.add(reversed);
//        mangles.add(word + word);
//        mangles.add(word + reversed);
//        mangles.add(reversed + word);
//
//        mangles.add("123" + word);
//        mangles.add(word + "123");
//
//        return mangles;
//    }
//
//    static String capitalize(String word) {
//        if (word.isEmpty()) return word;
//        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
//    }
//
//    static String ncapitalize(String word) {
//        if (word.isEmpty()) return word;
//        return Character.toLowerCase(word.charAt(0)) + word.substring(1).toUpperCase();
//    }
//
//    static String toggleCase(String word) {
//        StringBuilder sb = new StringBuilder();
//        boolean upper = true;
//        for (char c : word.toCharArray()) {
//            sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
//            upper = !upper;
//        }
//        return sb.toString();
//    }
//}
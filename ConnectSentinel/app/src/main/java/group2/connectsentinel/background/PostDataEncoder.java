package group2.connectsentinel.background;

public class PostDataEncoder {

    public static String encodePostData(String[] keys, String[] values) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < keys.length; i++) {
            result.append(keys[i]);
            result.append("=");
            result.append(values[i]);
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

}

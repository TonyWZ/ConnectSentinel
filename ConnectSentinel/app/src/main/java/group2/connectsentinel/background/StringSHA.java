package group2.connectsentinel.background;

import android.os.Message;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringSHA {

    private static final char[] byteToHexCharArr = "0123456789ABCDEF".toCharArray();

    public static String hashString(String input) {
        if(input == null)
            return null;
        MessageDigest md = null;
        try{
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.v("Hash", "No SHA 256 algorithm");
        }
        if(md == null) {
            Log.v("Hash", "Message Digest Instance is null");
        }
        byte[] inputByteArr = input.getBytes();
        byte[] hashByteArr =  md.digest(inputByteArr);
        char[] hexArr = new char[hashByteArr.length * 2];
        for (int i = 0; i < hashByteArr.length; i++) {
            byte b = hashByteArr[i];
            Log.v("Hash", "Byte is " + String.valueOf(b));
            int ind1 = b & 0x0F;
            int ind2 = (b & 0xF0) >>> 4;
            Log.v("Hash", "First index is " + ind1);
            Log.v("Hash", "Second index is " + ind2);
            hexArr[2 * i] = byteToHexCharArr[ind1];
            hexArr[2 * i + 1] = byteToHexCharArr[ind2];
            Log.v("Hash", "First hex is " + hexArr[2*i]);
            Log.v("Hash", "Second hex is " + hexArr[2*i+1]);
        }
        return new String(hexArr);
    }
}

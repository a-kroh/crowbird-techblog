/*
 * Copyright 2010 Andrew Kroh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.krohinc.cvs;

/**
 * A simple class for encoding and decoding passwords for CVS pserver protocol.
 * Can be used to recover forgotten passwords.
 * 
 * <p>
 * Adapted from: http://blog.zmeeagain.com/2005/01/recover-cvs-pserver-passwords.html
 */
public class CvsPassword
{
    private static final char[] LOOKUP_TABLE = 
           {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 114, 120, 53,
            79, 0, 109, 72, 108, 70, 64, 76, 67, 116, 74, 68, 87, 111, 52, 75,
            119, 49, 34, 82, 81, 95, 65, 112, 86, 118, 110, 122, 105, 41, 57,
            83, 43, 46, 102, 40, 89, 38, 103, 45, 50, 42, 123, 91, 35, 125, 55,
            54, 66, 124, 126, 59, 47, 92, 71, 115, 78, 88, 107, 106, 56, 0,
            121, 117, 104, 101, 100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48,
            58, 113, 32, 90, 44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85};

    /**
     * Encodes a CVS password to be used in .cvspass file. Throws an exception
     * if clearText is null, if a character is found outside the 0 - 126 range, or
     * if within the range, one of the non-allowed characters.
     * 
     * @param clearText
     *            the password in clear to be encoded
     *            
     * @return the encoded cvs password
     */
    public static String encode(String clearText)
    {
        // First character of encoded version is A:
        char[] encoded = new char[clearText.length() + 1];
        encoded[0] = 'A';
        
        // Skip the first character:
        int counter = 1;
        for (char c : clearText.toCharArray())
        {
            if (c == '`' || c == '$' || c < 32)
            {
                throw new IllegalArgumentException(
                        "Illegal character was found in clear password.");
            }
            
            encoded[counter++] = LOOKUP_TABLE[c];
        }

        return String.valueOf(encoded);
    }

    /**
     * Recovers an encoded via pserver protocol CVS password.
     * 
     * @param encodedPassword
     *            the encoded password to be decoded
     *            
     * @return the decoded password or null if the input was
     *      null or empty
     */
    public static String decode(String encodedPassword)
    {
        String rtn = null;
        
        if (encodedPassword != null && encodedPassword.length() > 0)
        {
            if (encodedPassword.startsWith("A"))
            {
                rtn = encode(encodedPassword.substring(1)).substring(1);
            }
            else
            {
                rtn = encode(encodedPassword).substring(1);
            }
        }
        
        return rtn;
    }
    
    public static void main(String[] sArgs)
    {
        final String TEST_WORD = "password";
        String encoded = CvsPassword.encode(TEST_WORD);
        System.out.println("Encoded: <" + encoded + ">");
        String decoded = CvsPassword.decode(encoded);
        System.out.println("Decoded: <" + decoded + ">");
        System.out.println(decoded.equals(TEST_WORD) ? "Test Passed" : "Test Failed");
    }
}


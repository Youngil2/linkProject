package com.practice.kyi.config;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Decrypt {
	
	private static final Logger logger = LoggerFactory.getLogger(Decrypt.class);

	//암호화 작업
    public static String decryptRsa(PrivateKey privateKey, String securedValue) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
   	 
    	Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    	byte[] encryptedBytes = hexToByteArray(securedValue);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, "utf-8");
	}
	
	public static byte[] hexToByteArray(String hex) {
	
	    if (hex == null || hex.length() % 2 != 0) {
	        return new byte[] {};
	    }
	
	    byte[] bytes = new byte[hex.length() / 2];
	    
	    logger.debug("mmLoginRSACtrl hexToByteArray bytes : " + bytes);
	
	    for (int i = 0; i < hex.length(); i += 2) {
	        byte value = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
	
	        bytes[(int) Math.floor(i / 2)] = value;
	    }
	
	    logger.debug("mmLoginRSACtrl hexToByteArray final bytes : " + bytes);
	
	    return bytes;
	}
}

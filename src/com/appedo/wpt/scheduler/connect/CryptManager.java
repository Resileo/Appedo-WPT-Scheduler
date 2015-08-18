package com.appedo.wpt.scheduler.connect;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.appedo.wpt.scheduler.manager.LogManager;
import com.sun.crypto.provider.SunJCE;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Class which handles the encryption and decryption operation required for this application.
 * Encryption is done with base64 encoder/decoder with a key.
 * 
 * @author	jenithmichael
 * @Purpose	Intialize Cipher with salt(Key), create salted password (Encrypt) , and Decrypt for UserAuthentication
 */

public class CryptManager {
	public static String CLASS_ID = "CryptManager";
	
	private static Cipher ecipher;
	private static Cipher dcipher;
	private static SecretKey key;
	
	private static final String ALGO = "AES";
	
	/** 
	* Initialize PasswordEncrypter with the SecretKey 
	*	@param	key:
	*/
	static {
		try {
			key = generateKey();
			
			ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new SunJCE());
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new SunJCE());
			
			byte[] iv = "ssit1234products".getBytes("UTF-8");
			
			ecipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			dcipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

		} catch (Exception e) {
			//	Logger.error(CLASS_ID,"Invalid Key.",e);
			LogManager.errorLog(e);
		}
	}
	
	/**
	 * Generate a encoded key with the password.
	 * 
	 * @return
	 * @throws Exception
	 */
	private static SecretKey generateKey() throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		char[] password = new char[] {'s', 's', '1', 't', '_', '0', 'p', '3', 'n', 'm', '3', 'n', 't', '0', 'r', '_', '_', 'p', '@', 's', 's', 'w', '0', 'r', 'd' };
		byte[] salt = new byte[] {'s', 's', '1', 't', '_', 'S', '@', '1', 't'};
		
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
		SecretKey tmp = factory.generateSecret(spec);
		byte[] encoded = tmp.getEncoded();
		
		return new SecretKeySpec(encoded, ALGO);
	}
	
	/** 
	* Encrypt the given string using global Cryption-Key available in this object.
	* 
	* @param
	*/
	public static String encrypt(String str) throws Exception {
		// Encode the string into bytes using utf-8
		byte[] utf8 = str.getBytes("UTF8");
		
		// Encrypt
		byte[] enc = ecipher.doFinal(utf8);
		
		// Encode bytes to base64 to get a string
		return new BASE64Encoder().encode(enc);
	}
	
	/**
	 * Encrypt the given string with the global Cryption-Key. And Encode it to use it in URL.
	 * 
	 * @param Data
	 * @return
	 * @throws Exception
	 */
	public static String encryptEncodeURL(String strValue) throws Exception {
		String encryptedValue = encrypt(strValue);
		
		encryptedValue = URLEncoder.encode(encryptedValue, "UTF-8");
		
		// presence of %2F in URL, throws request back as 400-Bad Request.
		encryptedValue = encryptedValue.replaceAll("%", "QAMPERAGE");
		
		return encryptedValue;
	}
	
	/** 
	* Decrypt the given string using global Cryption-Key
	* 
	* @param
	*/
	public synchronized static String decrypt(String str) throws Exception {
		// Decode base64 to get bytes
		byte[] dec = new BASE64Decoder().decodeBuffer(str);
		
		// Decrypt
		byte[] utf8 = dcipher.doFinal(dec);
		
		// Decode using utf-8
		return new String(utf8, "UTF8");
	}
	
	/**
	 * Decode the given URL. And Decode the encrypted input as normal String with the global Cryption-Key.
	 * 
	 * @param encryptedData
	 * @return
	 * @throws Exception
	 */
	public static String decodeDecryptURL(String encryptedData) throws Exception {
		// To avoid 400, %2F is replaced as QAM2F
		encryptedData = encryptedData.replaceAll("QAMPERAGE", "%");
		
		encryptedData = URLDecoder.decode(encryptedData, "UTF-8");
		
		// Decode using utf-8
		return decrypt(encryptedData);
	}
	
	
	
	/** 
	* Convert the given Key object to a String
	* 
	* @param
	*/
	private static String convertKeyToString(SecretKey key) {
		String keyString = null;
		byte[] keyBytes = null;
		
		try {
			 // Get encoded bytes of key	
			 keyBytes = key.getEncoded();
			//	Logger.info(CLASS_ID,"Generated Key Byte <=>"+keyBytes);
			 
			 // Encode bytes to base64 to get a string
			 keyString = new BASE64Encoder().encode(keyBytes);
			
		} catch (NullPointerException nex){
			//	Logger.error(CLASS_ID,"Error in convertiong key to string",nex);
		}
		
		return keyString;
	}
	
	
	/** 
	* Convert the given String as Key Object
	* 
	* @param	strKey:
	*/
	public static SecretKey convertStringToKey(String strKey) {
		SecretKey key = null;
		// Logger.info(CLASS_ID,"Retrieved Key String Buffer =>"+strKey);
	
		try {
			// Decode base64 to get bytes
			byte[] desKeyData = new BASE64Decoder().decodeBuffer(strKey);
		 //	Logger.info(CLASS_ID,"Received Key Byte <=> "+ desKeyData);
			
			// DES algorithm key specification instantiated with key byte
			DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
			
			// Get secretfactory instance with DES alogorithm
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			
			// generate secret key
			key = keyFactory.generateSecret(desKeySpec);

		} catch (NullPointerException nex){
			//	Logger.error(CLASS_ID,"Key string has invalide bytes.",nex);
		
		} catch (IOException iex){
			//	Logger.error(CLASS_ID,"Error in key retrieving.",iex);
		
		} catch (InvalidKeyException iex){
			// Logger.error(CLASS_ID,"Invalid Key.",iex);

		} catch (NoSuchAlgorithmException iex){
			//	Logger.error(CLASS_ID,"Invalide Algorithm.",iex);

		} catch (InvalidKeySpecException iex){
		 // Logger.error(CLASS_ID,"Invalid Key Specification.",iex);
		}
		
		//	Logger.info(CLASS_ID,"Generated Key Object =>"+key);
		return key;
	}
	
	public static void main(String args[]) throws Exception {
		LogManager.infoLog(encrypt("10"));
		LogManager.infoLog(decrypt("piyJabQwvLReQ2oPVC54fQ=="));
		LogManager.infoLog(decodeDecryptURL("A2RyStGARFu4G7asxzaRJQQAMPERAGE3DQAMPERAGE3D"));
	}
}

package com.example.vconference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * Singleton design, serialize
 */
public class Settings implements Serializable {
	private static final long serialVersionUID = 7330955796402400333L;
	private static final char[] PASSWORD = "enfldsgbnlsngdlksdsgm".toCharArray();
	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

	private static Settings instance = null;
	private static String settingFile;

	private boolean signInAutomatically;
	private String login;
	private String password;

	public int lastTab;
	
	public boolean contactShowUsers;
	
	public String getLogin() {
		return login;
	}

	public void setEmail(String email) {
		this.login = email;
	}

	public String getPassword() {
		return decrypt(password);
	}

	public boolean isSignInAutomatically() {
		return signInAutomatically;
	}

	public void setSignInAutomatically(boolean signInAutomatically, String login, String password) {
		this.signInAutomatically = signInAutomatically;
		if (!signInAutomatically) {
			this.login = login;
			this.password = null;
		} else {
			this.login = login;
			this.password = encrypt(password);
		}
	}
	
	public void failedSignIn() {
		if (signInAutomatically) {
			this.password = null;
			signInAutomatically = false;
		}
	}

	private static String encrypt(String property) {
		SecretKeyFactory keyFactory;
		try {
			keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
			return new String(Base64.encode(pbeCipher.doFinal(property.getBytes("UTF-8")), Base64.NO_WRAP));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

		// return null;//base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
	}

	private String decrypt(String value) {
		try {
			final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
			return new String(pbeCipher.doFinal(bytes), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Settings
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	// Deserialize settings.ser
	@SuppressWarnings("resource")
	public static Settings getInstance(Context context, String settingFile) {
		if (instance != null)
			return instance;
		FileInputStream fio;
		try {
			if (Settings.settingFile == null)
				Settings.settingFile = settingFile;

			File file = new File(settingFile);
			if (!file.exists()) {
				instance = getInstance();
				instance.saveSettings();
				return instance;
			}
			fio = new FileInputStream(settingFile);
			ObjectInputStream ois = new ObjectInputStream(fio);
			Object obj = ois.readObject();
			if (obj instanceof Settings) {
				instance = (Settings) obj;
				return getInstance();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveSettings() {
		try {
			if (settingFile != null || settingFile.trim().equals("")) {
				FileOutputStream fos = new FileOutputStream(settingFile);

				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(this);
				Log.e("save", "settings.ser made");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

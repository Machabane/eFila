package model.nonPersistent;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public  class  Autenticacao {

	
	
	public static String senhaTemporaria = "senhatemporaria"; 
	

	


	

	//Usada para encriptar a senha no momento do Login
	public static String converteMD5(String s) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       m.update(s.getBytes(),0,s.length());
	       
		return new BigInteger(1,m.digest()).toString(16);
	}


}

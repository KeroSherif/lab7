/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frontend.lab7;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Mohamed
 */
public class PasswordEncryption {
    public static String hashPassword(String password) throws NoSuchAlgorithmException{
       if(password == null || password.isEmpty()){
           throw new IllegalArgumentException("Please enter the password");
       }
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hashBytes);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }
            
            return hexString.toString();
       }catch(NoSuchAlgorithmException e){
           throw new RuntimeException("Error: Algorithim not found", e);
       }
    }
}

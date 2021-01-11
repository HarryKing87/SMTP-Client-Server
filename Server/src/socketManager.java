import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class socketManager {
          public Socket soc = null;
        
          public DataInputStream input = null;
          public DataOutputStream output = null;
          
          public socketManager(Socket socket) throws IOException {
                    soc = socket;
                    input = new DataInputStream(soc.getInputStream());
                    output = new DataOutputStream(soc.getOutputStream());
          }

          /**
         * Receive data from the remote host. The returned data from this function will be decrypted data.
         * @return
         * @throws Exception
         */
        public String recv() throws Exception {
          return decrypt(input.readUTF());
  }

  /**
   * Send data to the remote host. The data that is sent is encrypted before sending
   * @param s
   * @throws Exception
   */
  public void send(String s) throws Exception {
          output.writeUTF(encrypt(s));

  }

  public void disconnect() throws IOException {
          //close the input/output stream
          input.close();
          output.close();
          if (isConnected()){
                  soc.close();
          }
  }

  public boolean isConnected(){
          return soc!=null && !soc.isClosed();
  }

  // Following encryption and decryption functions are
  // based on https://stackoverflow.com/questions/24968466/how-to-use-cipher-on-this-method-to-decrypt-a-string

  //we'll use following data for the data encryption
  private static String secretKey="kdfslksdnflsdfsd";
  private static final String ALGORITHM = "Blowfish";
  private static final String MODE = "Blowfish/CBC/PKCS5Padding";
  private static final String IV = "abcdefgh";

  public static  String encrypt(String value) throws Exception{
          SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(MODE);
          cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
          byte[] values = cipher.doFinal(value.getBytes());
          return Base64.getEncoder().encodeToString(values);
  }

  public static  String decrypt(String value) throws Exception{
          byte[] values = Base64.getDecoder().decode(value);
          SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(MODE);
          cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
          return new String(cipher.doFinal(values));
  }
}


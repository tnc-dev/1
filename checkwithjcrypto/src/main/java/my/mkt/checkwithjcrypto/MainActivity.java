package my.mkt.checkwithjcrypto;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            //ключ
            PublicKey publickey= null;
            //получение генератора цифровой подписи
            Signature signatureDriver=Signature.getInstance("","ViPNet");
            //инициализация генератора для проверки подписи
            signatureDriver.initVerify(publickey);
           //обработка данных
            byte[] nextDataChuck=null;
            signatureDriver.update(nextDataChuck);
            //завершение проверки подписи
            byte[] signatureValue=null;
            boolean signatureVerified=signatureDriver.verify(signatureValue);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }
}

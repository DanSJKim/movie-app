package com.example.retrofitexample.BoxOffice;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.retrofitexample.R;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutionException;

public class WalletActivity extends AppCompatActivity {
    public static final String TAG = "WalletActivity : ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        setupBouncyCastle();

    }

    public String getBalance(String address)
    {
        //통신할 노드의 주소를 지정해준다.
        Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/c935c2faaee84cc0960768cff621de3a"));
        String result = null;
        EthGetBalance ethGetBalance = null;
        try {

            //이더리움 노드에게 지정한 Address 의 잔액을 조회한다.
            ethGetBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger wei = ethGetBalance.getBalance();

            //wei 단위를 ETH 단위로 변환 한다.
            result = Convert.fromWei(wei.toString() , Convert.Unit.ETHER).toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }



    public String[] createWallet(final String password) {
        String[] result = new String[2];
        try {
            Log.d(TAG, "createWallet: success");
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); //다운로드 path 가져오기
            if (!path.exists()) {
                path.mkdir();
            }
            String fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path))); //지갑생성
            result[0] = path+"/"+fileName;

            Credentials credentials = WalletUtils.loadCredentials(password,result[0]);

            result[1] = credentials.getAddress();
            Log.d(TAG, "createWallet: ");

            return result;
            
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | IOException
                | CipherException e) {
            Log.d(TAG, "createWallet: fail");
            e.printStackTrace();
            return null;
        }
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public void createWallet(View view){
        createWallet("12345");
    }

    public void balance(View view){
        getBalance("0xCB21F93bAA8569f134E265BDDf96270bB7B914bF");
        Log.d(TAG, "balance: " + getBalance("0xCB21F93bAA8569f134E265BDDf96270bB7B914bF"));
    }
}

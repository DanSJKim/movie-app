package com.example.retrofitexample.BoxOffice.Wallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import info.bcdev.librarysdkew.GetCredentials;
import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.LoadSmartContract;
import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.utils.qr.Generate;
import info.bcdev.librarysdkew.utils.qr.ScanIntegrator;
import info.bcdev.librarysdkew.wallet.Balance;
import info.bcdev.librarysdkew.wallet.SendingEther;
import info.bcdev.librarysdkew.wallet.SendingToken;
import info.bcdev.librarysdkew.wallet.generate.Bip44;
import info.bcdev.librarysdkew.web3j.Initiate;

/**
 *
 * @author Dmitry Markelov
 * Telegram group: https://t.me/joinchat/D62dXAwO6kkm8hjlJTR9VA
 *
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Если есть вопросы, отвечу в телеграме
 * If you have any questions, I will answer the telegram
 *
 *    Russian:
 *    Пример включает следующие функции:
 *       - Получаем адрес кошелька
 *       - Получаем баланс Eth
 *       - Получаем баланс Токена
 *       - Получаем название Токена
 *       - Получаем символ Токена
 *       - Получаем адрес Контракта Токена
 *       - Получаем общее количество выпущеных Токенов
 *
 *
 *   English:
 *   The example includes the following functions:
 *       - Get address wallet
 *       - Get balance Eth
 *       - Get balance Token
 *       - Get Name Token
 *       - Get Symbol Token
 *       - Get contract Token address
 *       - Get supply Token
 *
 */

public class WalletActivity extends AppCompatActivity implements CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {
    public static final String TAG = "WalletActivity : ";

    private String mNodeUrl = config.addressethnode(2); // 테스트넷 주소

    private String mPasswordwallet = config.passwordwallet(); // 지갑 비밀번호

    private String mSmartcontract = config.addresssmartcontract(1); // 스마트 컨트랙트 주소

    TextView ethaddress, ethbalance, tokenname, tokensymbol, tokensupply, tokenaddress, tokenbalance, tokensymbolbalance, seedcode;
    TextView tv_gas_limit, tv_gas_price, tv_fee;
    EditText sendtoaddress, sendtokenvalue, sendethervalue;

    ImageView qr_small, qr_big; // QR코드

    final Context context = this;

    IntentIntegrator qrScan;

    private Web3j mWeb3j; // 테스트넷 라이브러리

    private File keydir; // 키 경로

    private Credentials mCredentials;

    private InfoDialog mInfoDialog; // 알림 다이얼로그

    private BigInteger mGasPrice; // 가스 비용

    private BigInteger mGasLimit; // 가스 제한

    private SendingEther sendingEther; // 이더리움 보내기

    private SendingToken sendingToken; // 토큰 보내기

    private ToastMsg toastMsg; // 토스트 메세지

    private Button refresh; // 새로 고침

    BigInteger GAS = Contract.GAS_LIMIT;
    BigInteger GAS_PRICE = Contract.GAS_PRICE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Log.e(TAG, "onCreate: 앱 실행 3. ");

        mInfoDialog = new InfoDialog(this);

        ethaddress = (TextView) findViewById(R.id.ethaddress); // Your Ether Address
        ethbalance = (TextView) findViewById(R.id.ethbalance); // Your Ether Balance

        tokenname = (TextView) findViewById(R.id.tokenname); // Token Name
        tokensymbol = (TextView) findViewById(R.id.tokensymbol); // Token Symbol
        tokensupply = (TextView) findViewById(R.id.tokensupply); // Token Supply
        tokenaddress = (TextView) findViewById(R.id.tokenaddress); // Token Address
        tokenbalance = (TextView) findViewById(R.id.tokenbalance); // Token Balance
        tokensymbolbalance = (TextView) findViewById(R.id.tokensymbolbalance);
        seedcode = (TextView) findViewById(R.id.seedcode);

        sendtoaddress = (EditText) findViewById(R.id.sendtoaddress); // Address for sending ether or token

        sendtokenvalue = (EditText) findViewById(R.id.SendTokenValue); // Ammount token for sending
        sendethervalue = (EditText) findViewById(R.id.SendEthValue); // Ammount ether for sending

        refresh = (Button) findViewById(R.id.refresh); // 새로 고침

        qr_small = (ImageView)findViewById(R.id.qr_small);

        qrScan = new IntentIntegrator(this);

        tv_gas_limit = (TextView) findViewById(R.id.tv_gas_limit); // 가스 제한
        tv_gas_price = (TextView) findViewById(R.id.tv_gas_price); // 가스 비용
        tv_fee = (TextView) findViewById(R.id.tv_fee);

        final SeekBar sb_gas_limit = (SeekBar) findViewById(R.id.sb_gas_limit);
        sb_gas_limit.setOnSeekBarChangeListener(seekBarChangeListenerGL); // 가스 제한 seekbar
        final SeekBar sb_gas_price = (SeekBar) findViewById(R.id.sb_gas_price);
        sb_gas_price.setOnSeekBarChangeListener(seekBarChangeListenerGP); // 가스 비용 seekbar

        GetFee(); // 수수료 불러 오기

        getWeb3j(); // 테스트넷 연결 하기

        toastMsg = new ToastMsg(); // 토스트 메세지

        //keydir = this.getFilesDir("/keystore/");

        keydir = this.getFilesDir();
        Log.e(TAG, "onCreate: keydir: " + this.getFilesDir());

        File[] listfiles = keydir.listFiles();

        // 지갑 유무 확인
        if (listfiles.length == 0 ) {

            Log.e(TAG, "onCreate: 기존 지갑이 없음");
            CreateWallet();

        } else {

            Log.e(TAG, "onCreate: 앱 실행 6. else");
            Log.d(TAG, "onCreate: keydir: " + keydir);
            getCredentials(keydir);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.SendEther:
                Log.e(TAG, "onClick: SendEther");
                sendEther();
                break;
            case R.id.SendToken:
                Log.e(TAG, "onClick: SendToken");
                sendToken();
                break;
            case R.id.qr_small:
                Log.e(TAG, "onClick: qr_small");
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.qr_view);
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(new Generate().Get(getEthAddress(),600,600));
                dialog.show();
                break;
            case R.id.qrScan:
                Log.e(TAG, "onClick: qrScan");
                new ScanIntegrator(this).startScan();
                break;
            case R.id.refresh:
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
                LoadWallet();
                break;
        }
    }

    /* Create Wallet */
    private void CreateWallet(){
        Log.e(TAG, "CreateWallet: 지갑 생성");

        Bip44 bip44 = new Bip44();
        bip44.registerCallBack(this);
        bip44.execute(mPasswordwallet);
        mInfoDialog.Get("Wallet generation", "Please wait few seconds");
    }

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        Log.e(TAG, "backGeneration: ");

        mCredentials = credentials;
        setEthAddress(result.get("address")); // 이더리움 주소 설정
        setEthBalance(getEthBalance()); // 이더리움 잔액 설정
        setSeed(result.get(seedcode));
        new SaveWallet(keydir,mCredentials,mPasswordwallet).execute();
        mInfoDialog.Dismiss();
    }

    private void setSeed(String seed){
        Log.e(TAG, "setSeed: ");
        seedcode.setText(seed);
    }
    /* End Create Wallet*/

    /* Get Web3j*/
    private void getWeb3j(){
        Log.e(TAG, "getWeb3j: 앱 실행 5. 이더리움 테스트넷 연결을 위한 라이브러리 실행");
        new Initiate(mNodeUrl); // 테스트넷 주소 설정
        mWeb3j = Initiate.sWeb3jInstance;
    }

    /* Get Credentials */
    private void getCredentials(File keydir){
        File[] listfiles = keydir.listFiles();
        try {
            Log.e(TAG, "getCredentials: 앱 실행 6.");
            Log.d(TAG, "getCredentials: listfiles: " + listfiles[0]);
            mInfoDialog.Get("Load Wallet","Please wait few seconds");
            GetCredentials getCredentials = new GetCredentials();
            getCredentials.registerCallBack(this);
            getCredentials.FromFile(listfiles[0].getAbsolutePath(),mPasswordwallet);
        } catch (IOException e) {

            Log.e(TAG, "getCredentials: IOException " + e);
            e.printStackTrace();
        } catch (CipherException e) {

            Log.e(TAG, "getCredentials: CipherException " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void backLoadCredential(Credentials credentials) {
        Log.e(TAG, "backLoadCredential: 앱 실행 6. 지갑을 불러 온다.");
        mCredentials = credentials;
        Log.e(TAG, "backLoadCredential: mCredentials: " + mCredentials);
        mInfoDialog.Dismiss();
        LoadWallet();
    }
    /* End Get Credentials */

    private void LoadWallet(){
        Log.e(TAG, "LoadWallet: 앱 실행 7. 지갑을 불러 온다.");
        setEthAddress(getEthAddress()); // 이더리움 주소 불러오기
        setEthBalance(getEthBalance()); // 이더리움 잔액 불럴오기
        GetTokenInfo();
    }

    /* Get Address Ethereum */
    private String getEthAddress(){
        Log.e(TAG, "getEthAddress: 앱 실행 8. 이더리움 주소를 불러 온다. " + mCredentials.getAddress());

        SharedPref.getInstance(WalletActivity.this).storeEthAddress(mCredentials.getAddress()); // save eth address into SharedPreferences
        return mCredentials.getAddress();
    }

    /* Set Address Ethereum */
    private void setEthAddress(String address){
        Log.e(TAG, "setEthAddress: 이더리움 주소를 설정 한다.");

        ethaddress.setText(address);
        qr_small.setImageBitmap(new Generate().Get(address,200,200));
    }

    private String getToAddress(){
        Log.e(TAG, "getToAddress: " + sendtoaddress.getText().toString());

        return sendtoaddress.getText().toString();
    }

    private void setToAddress(String toAddress){
        Log.e(TAG, "setToAddress: ");

        sendtoaddress.setText(toAddress);
    }

    /* Get Balance */
    private String getEthBalance(){
        try {
            Log.e(TAG, "getEthBalance: 앱 실행 4. 이더리움 잔액 불러 오기");

            return new Balance(mWeb3j,getEthAddress()).getInEther().toString();
        } catch (ExecutionException e) {
            Log.e(TAG, "getEthBalance: ExecutionException " + e);

            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, "getEthBalance: InterruptedException " + e);
            e.printStackTrace();
        }
        return null;
    }

    /* Get Send Ammount */
    private String getSendEtherAmmount(){
        Log.e(TAG, "getSendEtherAmmount: " + sendethervalue.getText().toString());
        return sendethervalue.getText().toString();
    }

    private String getSendTokenAmmount(){
        Log.e(TAG, "getSendTokenAmmount: " + sendtokenvalue.getText().toString());
        return sendtokenvalue.getText().toString();
    }

    /* Set Balance */
    private void setEthBalance(String ethBalance){
        Log.e(TAG, "setEthBalance: " + ethBalance);

        ethbalance.setText(ethBalance);
    }

    // 앱 첫 실행, 이더리움, 토큰 보낼 때 실행 되는 메소드
    public void GetFee(){
        Log.e(TAG, "GetFee: 앱 실행 4. Gas(수수료) 설정 메소드");

        setGasPrice(GAS_PRICE.toString());
        setGasLimit(GAS.toString());

        BigDecimal fee = BigDecimal.valueOf(mGasPrice.doubleValue()*mGasLimit.doubleValue());
        Log.e(TAG, "GetFee: fee: " + fee);
        BigDecimal feeresult = Convert.fromWei(fee.toString(),Convert.Unit.ETHER);
        Log.e(TAG, "GetFee: feeresult: " + feeresult);
        tv_fee.setText(feeresult.toPlainString() + " ETH");
        Log.e(TAG, "GetFee: feeresult.toPlainString(): " + feeresult.toPlainString());
    }

    private String getGasPrice(){
        Log.e(TAG, "getGasPrice: 가스 비용 불러 오기 " + tv_gas_price.getText().toString());
        return tv_gas_price.getText().toString();
    }

    private void setGasPrice(String gasPrice){
        Log.e(TAG, "setGasPrice: 가스 비용 설정 하기 " + mGasPrice);
        mGasPrice = Convert.toWei(gasPrice,Convert.Unit.GWEI).toBigInteger();
    }

    private String getGasLimit() {
        Log.e(TAG, "getGasLimit: 가스 제한 값 불러 오기 " + tv_gas_limit.getText().toString());
        return tv_gas_limit.getText().toString();
    }

    private void setGasLimit(String gasLimit){
        Log.e(TAG, "setGasLimit: 가스 제한 설정 하기 " + mGasLimit);
        mGasLimit = BigInteger.valueOf(Long.valueOf(gasLimit));
    }

    /*Get Token Info*/
    private void GetTokenInfo(){
        Log.e(TAG, "GetTokenInfo: " + "앱 실행 9. 토큰 정보 가져 오기 ");
        LoadSmartContract loadSmartContract = new LoadSmartContract(mWeb3j,mCredentials,mSmartcontract,mGasPrice,mGasLimit);
        Log.e(TAG, "GetTokenInfo: mSmartcontract: " + mSmartcontract);
        Log.e(TAG, "GetTokenInfo: mGasPrice: " + mGasPrice);
        Log.e(TAG, "GetTokenInfo: mGasLimit: " + mGasLimit);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }

    /* Get Token*/
    @Override
    public void backLoadSmartContract(Map<String,String> result) {
        Log.e(TAG, "backLoadSmartContract: ");

        setTokenBalance(result.get("tokenbalance")); // 토큰 잔액 설정
        setTokenName(result.get("tokenname")); // 토큰 이름 설정
        setTokenSymbol(result.get("tokensymbol")); //
        setTokenAddress(result.get("tokenaddress"));
        setTokenSupply(result.get("totalsupply"));
    }

    private void setTokenBalance(String value){
        Log.e(TAG, "setTokenBalance: 토큰 잔액 설정 " + value);
        //BigDecimal balance = Convert.fromWei(value,Convert.Unit.ETHER);
        tokenbalance.setText(value);
    }

    private void setTokenName(String value){
        Log.e(TAG, "setTokenName: 토큰 이름 설정 " + value);
        tokenname.setText(value);
    }

    private void setTokenSymbol(String value){
        Log.e(TAG, "setTokenSymbol: 토큰 심볼 설정 " + value);
        tokensymbol.setText(value);
    }

    private void setTokenSupply(String value){
        Log.e(TAG, "setTokenSupply: 토큰 총액 설정 " + value);
        //BigDecimal balance = Convert.fromWei(value,Convert.Unit.ETHER);
        tokensupply.setText(value);
    }

    private void setTokenAddress(String value){
        Log.e(TAG, "setTokenAddress: 토큰 주소 설정 " + value);
        tokenaddress.setText(value);
    }
    /* End Get Token*/

    /* Sending */
    private void sendEther(){
        Log.e(TAG, "sendEther: 이더리움 전송");

        sendingEther = new SendingEther(mWeb3j,
                mCredentials,
                getGasPrice(),
                getGasLimit());
        sendingEther.registerCallBack(this);
        sendingEther.Send(getToAddress(),getSendEtherAmmount());
    }

    @Override
    public void backSendEthereum(EthSendTransaction result) {
        Log.e(TAG, "backSendEthereum: ");

        toastMsg.Long(this,result.getTransactionHash());
        LoadWallet();
    }

    private void sendToken(){
        Log.e(TAG, "sendToken: 토큰 전송 1.");

        sendingToken = new SendingToken(mWeb3j,
                mCredentials,
                getGasPrice(),
                getGasLimit());
        sendingToken.registerCallBackToken(this);
        sendingToken.Send(mSmartcontract,getToAddress(),getSendTokenAmmount());
    }

    @Override
    public void backSendToken(TransactionReceipt result) {
        Log.e(TAG, "backSendToken: " + result);

        toastMsg.Long(this,result.getTransactionHash());
        LoadWallet();
    }
    /* End Sending */

    /* QR Scan */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.e(TAG, "onActivityResult: result.getContents() == null");

                toastMsg.Short(this, "Result Not Found");
            } else {
                Log.e(TAG, "onActivityResult: result.getContents() == null else");

                setToAddress(result.getContents());
                toastMsg.Short(this, result.getContents());
            }
        } else {
            Log.e(TAG, "onActivityResult: else");

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /* End Q Scan */

    /* SeekBar Listener */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGL = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasLimit(String.valueOf(seekBar.getProgress()*1000+42000));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGP = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasPrice(String.valueOf(seekBar.getProgress()+12));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };

    public void GetGasLimit(String value) {
        Log.e(TAG, "GetGasLimit: " + value);

        tv_gas_limit.setText(value);
        GetFee();
    }
    public void GetGasPrice(String value) {
        Log.e(TAG, "GetGasPrice: " + value);

        tv_gas_price.setText(value);
        GetFee();
    }


    /* End SeekBar Listener */
}

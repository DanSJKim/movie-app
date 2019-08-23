package info.bcdev.librarysdkew.wallet;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.utils.Convert;

import java.math.BigInteger;

import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.TokenERC20;

public class SendingToken {
    public static final String TAG = "SendingToken : ";

    private Credentials mCredentials;
    private Web3j mWeb3j;
    private String fromAddress;
    private String mValueGasPrice;
    private String mValueGasLimit;

    private CBSendingEther cbSendingEther;
    private CBSendingToken cbSendingToken;

    BigInteger GAS = Contract.GAS_LIMIT;
    BigInteger GAS_PRICE = Contract.GAS_PRICE;

    public SendingToken(Web3j web3j, Credentials credentials, String valueGasPrice, String valueGasLimit){

        Log.e(TAG, "SendingToken: 토큰 전송 2. (WalletActivity sendToken()에서 호출)" );
        mWeb3j = web3j;
        mCredentials = credentials;
        fromAddress = credentials.getAddress(); // 보내는 쪽의 주소
        mValueGasPrice = valueGasPrice; // 가스 가격
        mValueGasLimit = valueGasLimit; // 가스 제한

        Log.e(TAG, "SendingToken: mWeb3j: " + mWeb3j);
        Log.e(TAG, "SendingToken: mCredentials: " + mCredentials);
        Log.e(TAG, "SendingToken: fromAddress: " + fromAddress);
        Log.e(TAG, "SendingToken: mValueGasPrice: " + mValueGasPrice);
        Log.e(TAG, "SendingToken: mValueGasLimit: " + mValueGasLimit);


    }

    private BigInteger getGasPrice(){
        Log.e(TAG, "getGasPrice: 보낼 가스 값 " + BigInteger.valueOf(Long.valueOf(mValueGasPrice)));
        return BigInteger.valueOf(Long.valueOf(mValueGasPrice));
    }

    private BigInteger getGasLimit(){
        Log.e(TAG, "getGasLimit: 가스 한도 " + BigInteger.valueOf(Long.valueOf(mValueGasLimit)));
        return BigInteger.valueOf(Long.valueOf(mValueGasLimit));
    }

    public void Send(String smartContractAddress, String toAddress, String valueAmmount) {
        Log.e(TAG, "Send: 토큰 보내기 ");
        Log.e("SendingToken", "Send: smartContractAddress: " + smartContractAddress);
        Log.e("SendingToken", "Send: toAddress: " + toAddress);
        Log.e("SendingToken", "Send: valueAmmount: " + valueAmmount);
        new SendToken().execute(smartContractAddress,toAddress,valueAmmount);
    }

    private class SendToken extends AsyncTask<String,Void,TransactionReceipt> {

        @Override

        protected void onPreExecute() {
            Log.e(TAG, "onPreExecute: SendToken start");

        }

        @Override
        protected TransactionReceipt doInBackground(String... value) {

            //BigInteger ammount = BigInteger.valueOf(Long.parseLong(value[2]));
            BigInteger ammount = Convert.toWei(value[2], Convert.Unit.ETHER).toBigInteger();
            Log.e("SendingToken", "doInBackground: " + ammount);

            Log.e(TAG, "doInBackground: value[0] smartcontract: " + value[0]);
            Log.e(TAG, "doInBackground: value[1] 받는 주소: " + value[1]);
            Log.e(TAG, "doInBackground: value[2] 보내는 액수: " + value[2]);
            Log.e(TAG, "doInBackground: GAS_PRICE: " + GAS_PRICE);
            Log.e(TAG, "doInBackground: GAS: " + GAS);

            TokenERC20 token = TokenERC20.load(value[0], mWeb3j, mCredentials, GAS_PRICE, GAS);
            Log.e(TAG, "doInBackground: token: " + token);
            try {
                TransactionReceipt result = token.transfer(value[1], ammount).send();
                Log.e("SendingToken", "doInBackground: result: " + result);
                return result;
            } catch (Exception e) {
                Log.e("SendingToken", "doInBackground: error: " + e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(TransactionReceipt result) {
            super.onPostExecute(result);
            Log.e("SendingToken", "onPostExecute: " + result);
            cbSendingToken.backSendToken(result);
        }
    }

    public void registerCallBackToken(CBSendingToken cbSendingToken){
        Log.e("SendingToken", "registerCallBackToken: " + cbSendingToken);
        this.cbSendingToken = cbSendingToken;
    }

}

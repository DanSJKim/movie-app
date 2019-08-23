package info.bcdev.librarysdkew.smartcontract;

import android.os.AsyncTask;
import android.util.Log;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.Contract;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;

public class LoadSmartContract {
    public static final String TAG = "LoadSmartContract : ";

    private CBLoadSmartContract cbLoadSmartContract;
    private Web3j mWeb3j;
    private Credentials mCredentials;
    private String mSmartContractAddress;
    private BigInteger mGasPrice;
    private BigInteger mGasLimit;

    BigInteger GAS = Contract.GAS_LIMIT;
    BigInteger GAS_PRICE = Contract.GAS_PRICE;

    public LoadSmartContract(Web3j web3j,
                             Credentials credentials,
                             String smartContractAddress,
                             BigInteger gasPrice,
                             BigInteger gasLimit){
        mWeb3j = web3j;
        mCredentials = credentials;
        mSmartContractAddress = smartContractAddress;
        mGasPrice = gasPrice;
        mGasLimit = gasLimit;

        Log.d(TAG, "LoadSmartContract: mWeb3j: " + mWeb3j);
        Log.d(TAG, "LoadSmartContract: mCredentials: " + mCredentials);
        Log.d(TAG, "LoadSmartContract: mSmartContractAddress: " + mSmartContractAddress);
        Log.d(TAG, "LoadSmartContract: mGasPrice: " + mGasPrice);
        Log.d(TAG, "LoadSmartContract: mGasLimit: " + mGasLimit);

    }

    public void LoadToken(){
        Log.e("LoadSmartContract", "LoadToken: 앱 실행 11. 토큰 추가하기");
        new Token().execute();
    }

    private class Token extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Log.e(TAG, "doInBackground: try 앱 실행 12. ");
                String address = mCredentials.getAddress();
                Log.e("LoadSmartContract", "doInBackground: address: " + address);

                /**
                 // Получаем Баланс
                 // Get balance Ethereum
                 */
                EthGetBalance etherbalance = mWeb3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                System.out.println("Eth Balance: " + ethbalance);

                /**
                 // Загружаем Токен
                 // Download Token
                 */
                TokenERC20 token = TokenERC20.load(mSmartContractAddress, mWeb3j, mCredentials, GAS_PRICE, GAS);

                String tokenname = token.name().send();
                Log.d(TAG, "doInBackground: tokenname: " + tokenname);

                String tokensymbol = token.symbol().send();
                Log.d(TAG, "doInBackground: tokensymbol: " + tokensymbol);

                String tokenaddress = token.getContractAddress();
                Log.d(TAG, "doInBackground: tokenaddress: " + tokenaddress);

                BigInteger totalsupply = token.totalSupply().send();
                Log.d(TAG, "doInBackground: totalsupply: " + totalsupply);

                BigInteger tokenbalance = token.balanceOf(address).send();
                Log.d(TAG, "doInBackground: tokenbalance: " + tokenbalance);

                Map<String,String> result = new HashMap<>();
                result.put("tokenname",tokenname);
                result.put("tokensymbol",tokensymbol);
                result.put("tokenaddress",tokenaddress);
                result.put("totalsupply",totalsupply.toString());
                result.put("tokenbalance",tokenbalance.toString());

                return result;
            } catch (Exception ex) {
                Log.e(TAG, "doInBackground: error! " + ex);
                System.out.println("ERROR:" + ex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            super.onPostExecute(result);
            if (result != null){
                Log.e(TAG, "onPostExecute: 앱 실행 13. 토큰 추가하기 결과: " + result);
                cbLoadSmartContract.backLoadSmartContract(result);
            }
        }
    }

    public void registerCallBack(CBLoadSmartContract cbLoadSmartContract){
        Log.e(TAG, "registerCallBack: 앱 실행 10. " + cbLoadSmartContract);
        this.cbLoadSmartContract = cbLoadSmartContract;
    }
}

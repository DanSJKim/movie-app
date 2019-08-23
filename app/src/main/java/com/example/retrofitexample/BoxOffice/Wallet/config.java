package com.example.retrofitexample.BoxOffice.Wallet;

import android.util.Log;

public class config {
    public static final String TAG = "config : ";

    public static String addressethnode(int node) {
        switch(node){ // 테스트넷 주소
            case 1:
                Log.e(TAG, "addressethnode: 앱 실행 1. case 1");
                return "http://176.74.13.102:18087";
            case 2:
                Log.e(TAG, "addressethnode: 앱 실행 1. case 2");
                //return "http://192.168.0.33:8547";
                return "https://rinkeby.infura.io/v3/c935c2faaee84cc0960768cff621de3a";
            default:
                Log.e(TAG, "addressethnode: 앱 실행 1. default");
                return "https://mainnet.infura.io/avyPSzkHujVHtFtf8xwY";
        }
    }

    public static String addresssmartcontract(int contract) {
        switch (contract){
            case 1:
                Log.e(TAG, "addresssmartcontract: 앱 실행 2. case 1");
                //return "0x5C456316Da36c1c769FA277cE677CB8F690c5767";
                return "0xf4DD0fE2F32FC35595a3b75916f2DEC46DB6c479";
            default :
                Log.e(TAG, "addresssmartcontract: 앱 실행 2. default");
                return "0x89205A3A3b2A69De6Dbf7f01ED13B2108B2c43e7";
        }
    }

    public static String passwordwallet() {
        Log.e(TAG, "passwordwallet: ");
        return "";
    }


}

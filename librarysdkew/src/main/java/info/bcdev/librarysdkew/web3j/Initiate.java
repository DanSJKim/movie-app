package info.bcdev.librarysdkew.web3j;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

public class Initiate {

    public static Web3j sWeb3jInstance;

    public Initiate(String urlNode){
        sWeb3jInstance = Web3jFactory.build(new HttpService(urlNode));

    }
}

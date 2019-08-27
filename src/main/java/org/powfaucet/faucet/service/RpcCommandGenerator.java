package org.powfaucet.faucet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RpcCommandGenerator {

    private final ObjectMapper mapper;

    public RpcCommandGenerator(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    public String getAddressInfo(String address) {
        ObjectNode getAddressInfoRpc = mapper.createObjectNode();
        getAddressInfoRpc.put("jsonrpc", "1.0");
        getAddressInfoRpc.put("method", "getaddressinfo");
        ArrayNode params = getAddressInfoRpc.putArray("params");
        params.add(address);
        return getAddressInfoRpc.toString();
    }

    public String getBalance() {
        ObjectNode getBalance = mapper.createObjectNode();
        getBalance.put("jsonrpc", "1.0");
        getBalance.put("method", "getbalance");
        return getBalance.toString();
    }

    public String sendToAddress(String address, BigDecimal amount) {
        ObjectNode getAddressInfoRpc = mapper.createObjectNode();
        getAddressInfoRpc.put("jsonrpc", "1.0");
        getAddressInfoRpc.put("method", "sendtoaddress");
        ArrayNode params = getAddressInfoRpc.putArray("params");
        params.add(address);
        params.add(amount.toPlainString());
        return getAddressInfoRpc.toString();
    }
}
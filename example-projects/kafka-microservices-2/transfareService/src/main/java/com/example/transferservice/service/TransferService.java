package com.example.transferservice.service;

import com.example.transferservice.model.TransferRestModel;

public interface TransferService {
    boolean transfer(TransferRestModel productPaymentRestModel);
    boolean transferV2(TransferRestModel transferRestModel);
}

package com.railgraph.controller;

import com.railgraph.dto.input.TransactionInputDTO;
import com.railgraph.dto.output.TransactionOutputDTO;
import com.railgraph.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping
    public TransactionOutputDTO createTransaction(@RequestBody TransactionInputDTO transactionInput) {
        return transactionService.createTransaction(transactionInput);
    }
    
    @GetMapping
    public List<TransactionOutputDTO> getAllTransactions() {
        return transactionService.getAllTransactions();
    }
    
    @GetMapping("/{id}")
    public TransactionOutputDTO getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }
    
    @GetMapping("/user/{userId}")
    public List<TransactionOutputDTO> getTransactionsByUser(@PathVariable("userId") String userId) {
        return transactionService.getTransactionsByUser(userId);
    }
}
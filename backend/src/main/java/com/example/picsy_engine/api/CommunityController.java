package com.example.picsy_engine.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.picsy_engine.api.dto.AddMemberRequest;
import com.example.picsy_engine.api.dto.CompanyCreateRequest;
import com.example.picsy_engine.api.dto.DecomposeResponse;
import com.example.picsy_engine.api.dto.InitializeRequest;
import com.example.picsy_engine.api.dto.RecoveryRequest;
import com.example.picsy_engine.api.dto.StateResponse;
import com.example.picsy_engine.api.dto.TransactionRequest;
import com.example.picsy_engine.api.dto.UpdateMatrixRequest;
import com.example.picsy_engine.service.ActionLogService;
import com.example.picsy_engine.service.SimulationService;

import jakarta.validation.Valid;

/**
 * REST API の入り口。
 *  - /api/state
 *  - /api/matrix (PUT)
 *  - /api/recovery (POST)
 *  - /api/transactions (POST)
 *  - /api/members (POST)
 *  - /api/members/{id}/ghost (POST)
 *  - /api/companies (POST)
 *  - /api/companies/{companyId}/decompose (GET)
 *  - /api/logs (GET)
 *
 * フロント(Vue)はこのAPI群だけ叩けば完成する。
 */
@RestController
@RequestMapping(path="/api", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins={"http://localhost:5173","http://localhost:5174"})
public class CommunityController {

    private final SimulationService service;
    private final ActionLogService logs;

    public CommunityController(SimulationService service, ActionLogService logs){
        this.service=service; this.logs=logs;
    }

    @GetMapping("/state")
    public StateResponse state(){ return service.getState(); }

    @PutMapping("/matrix")
    public StateResponse updateMatrix(@Valid @RequestBody UpdateMatrixRequest req){
        return service.updateMatrix(req);
    }

    @PostMapping("/recovery")
    public StateResponse recovery(@Valid @RequestBody RecoveryRequest req){
        return service.recover(req.gamma());
    }

    @PostMapping("/transactions")
    public StateResponse transact(@Valid @RequestBody TransactionRequest req){
        return service.transact(req);
    }

    @PostMapping("/members")
    public StateResponse addMember(@Valid @RequestBody AddMemberRequest req){
        return service.addMember(req);
    }

    @PostMapping("/members/{id}/ghost")
    public StateResponse ghost(@PathVariable int id){ return service.ghost(id); }

    @PostMapping("/companies")
    public StateResponse createCompany(@Valid @RequestBody CompanyCreateRequest req){
        return service.createCompany(req);
    }

    @GetMapping("/companies/{companyId}/decompose")
    public DecomposeResponse decompose(@PathVariable int companyId){
        return service.decomposeCompany(companyId);
    }

    @GetMapping("/logs")
    public List<String> logs(){ return logs.list(); }
    // 先頭の import 群に足す：


        // 初期化：人数と評価行列（names は任意）を受け取り、状態を全置換する
        @PostMapping("/initialize")
        public StateResponse initialize(@Valid @RequestBody InitializeRequest req){
            return service.initialize(req);
        }

}

package com.example.picsy_engine.api;

import com.example.picsy_engine.api.dto.*;
import com.example.picsy_engine.service.ActionLogService;
import com.example.picsy_engine.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}

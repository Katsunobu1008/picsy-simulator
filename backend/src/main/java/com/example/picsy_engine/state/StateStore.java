package com.example.picsy_engine.state;

import com.example.picsy_engine.domain.Member;
import com.example.picsy_engine.domain.MemberStatus;
import com.example.picsy_engine.domain.MemberType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * アプリの唯一の「真実」を持つインメモリの状態ストア。
 * - 行列 E（常に行和=1に保つ）
 * - メンバー一覧（安定ID）
 * - ID ↔ 行列index の写像
 *
 * DB不要のモノリス初期段階ではこれで十分。将来は差し替え可能。
 */
@Component
public class StateStore {

    private double[][] E; // 評価行列（row-sum=1）
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private final Map<Integer, Integer> idToIndex = new LinkedHashMap<>();
    private final List<Integer> indexToId = new ArrayList<>();
    private final AtomicInteger idSeq = new AtomicInteger(1);

    public StateStore(){
        // 初期3人 A,B,C
        int a=idSeq.getAndIncrement(), b=idSeq.getAndIncrement(), c=idSeq.getAndIncrement();
        members.put(a,new Member(a,"A", MemberType.PERSON));
        members.put(b,new Member(b,"B", MemberType.PERSON));
        members.put(c,new Member(c,"C", MemberType.PERSON));
        indexToId.add(a); indexToId.add(b); indexToId.add(c);
        idToIndex.put(a,0); idToIndex.put(b,1); idToIndex.put(c,2);

        // 行和=1の初期行列
        E = new double[][]{
            {0.40,0.30,0.30},
            {0.20,0.30,0.50},
            {0.25,0.45,0.30}
        };
    }

    public synchronized int size(){ return indexToId.size(); }

    public synchronized List<Member> listMembers(){
        List<Member> out = new ArrayList<>();
        for(int id:indexToId) out.add(members.get(id));
        return out;
    }

    public synchronized double[][] matrixCopy(){
        int n=E.length; double[][] m=new double[n][n];
        for(int i=0;i<n;i++) System.arraycopy(E[i],0,m[i],0,n);
        return m;
    }

    public synchronized void replaceMatrix(double[][] newE){
        if(newE.length!=indexToId.size()) throw new IllegalArgumentException("matrix size mismatch");
        this.E = newE;
    }

    public synchronized Optional<Integer> indexOfId(int id){
        return Optional.ofNullable(idToIndex.get(id));
    }

    public synchronized int idAtIndex(int index){ return indexToId.get(index); }

    /** 新IDを払い出し（indexは attach 時に確定） */
    public synchronized int allocateId(String name, boolean company){
        int id = idSeq.getAndIncrement();
        members.put(id, new Member(id, name, company? MemberType.COMPANY: MemberType.PERSON));
        return id;
    }

    /** 行列を N→N+1 に拡張した後、新IDに index を紐付ける */
    public synchronized void attachNewIndexForId(int newId, double[][] expandedE){
        int newIndex = expandedE.length-1;
        this.E = expandedE;
        idToIndex.put(newId,newIndex);
        indexToId.add(newId);
    }

    public synchronized void markGhost(int memberId){
        Member m = members.get(memberId);
        if(m!=null) m.setStatus(MemberStatus.GHOST);
    }

    public synchronized boolean isGhostRow(int rowIndex){
        int id = indexToId.get(rowIndex);
        return members.get(id).getStatus()==MemberStatus.GHOST;
    }
}

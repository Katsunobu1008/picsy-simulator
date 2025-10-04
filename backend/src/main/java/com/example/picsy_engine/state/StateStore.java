package com.example.picsy_engine.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;                    // ← resetWith(names, ...) で使う
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.example.picsy_engine.domain.Member;
import com.example.picsy_engine.domain.MemberStatus;
import com.example.picsy_engine.domain.MemberType;

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

    /** 評価行列（row-sum=1 を維持） */
    private double[][] E;

    /** ID→Member */
    private final Map<Integer, Member> members = new LinkedHashMap<>();

    /** ID→index の写像（行列 E の行/列に対応） */
    private final Map<Integer, Integer> idToIndex = new LinkedHashMap<>();

    /** index→ID の逆写像（描画や並びの基準） */
    private final List<Integer> indexToId = new ArrayList<>();

    /** 新しいID払い出し用のカウンタ（1始まり） */
    private final AtomicInteger idSeq = new AtomicInteger(1);

    /** コンストラクタ：初期3人（A,B,C）＋初期行列をセット */
    public StateStore(){
        // 初期3人 A,B,C を作成し、それぞれに永続IDを割り当てる
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

    /** メンバー数（= 行列サイズ） */
    public synchronized int size(){ return indexToId.size(); }

    /** indexToId の順でメンバー一覧を返す（表示用） */
    public synchronized List<Member> listMembers(){
        List<Member> out = new ArrayList<>();
        for(int id:indexToId) out.add(members.get(id));
        return out;
    }

    /** 行列 E のディープコピーを返す（外部から書き換えられないようにする） */
    public synchronized double[][] matrixCopy(){
        int n=E.length; double[][] m=new double[n][n];
        for(int i=0;i<n;i++) System.arraycopy(E[i],0,m[i],0,n);
        return m;
    }

    /** 行列を置換（サイズは現在の N と一致していること） */
    public synchronized void replaceMatrix(double[][] newE){
        if(newE.length!=indexToId.size()) throw new IllegalArgumentException("matrix size mismatch");
        this.E = newE;
    }

    /** 永続ID → 行列 index への写像 */
    public synchronized Optional<Integer> indexOfId(int id){
        return Optional.ofNullable(idToIndex.get(id));
    }

    /** index → 永続ID の逆写像 */
    public synchronized int idAtIndex(int index){ return indexToId.get(index); }

    /** 新IDを払い出し（index は attach 時に確定） */
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

    /** 指定IDをゴースト状態にする（自然回収対象外） */
    public synchronized void markGhost(int memberId){
        Member m = members.get(memberId);
        if(m!=null) m.setStatus(MemberStatus.GHOST);
    }

    /** 行番号 rowIndex（0-based）のメンバーがゴーストかどうか */
    public synchronized boolean isGhostRow(int rowIndex){
        int id = indexToId.get(rowIndex);
        return members.get(id).getStatus()==MemberStatus.GHOST;
    }

    /**
     * 初期化：与えられた names と正規化済み matrix で、状態を「全置換」する。
     * - 既存メンバーや行列は破棄し、新しい ID を払い出す（idSeq は続きから）
     * - すべて PERSON / ACTIVE で作る（会社はここでは作らない）
     * - matrix は呼び出し側（Service）で正方チェック・正規化済みであること
     */
    public synchronized void resetWith(List<String> names, double[][] normalizedMatrix){
        // 既存をクリア
        this.members.clear();
        this.idToIndex.clear();
        this.indexToId.clear();

        final int n = normalizedMatrix.length;
        this.E = normalizedMatrix; // 正規化済みをそのまま採用

        for (int i=0;i<n;i++){
            final String name;
            if (names == null || names.size() != n || names.get(i) == null || names.get(i).isBlank()){
                name = autoName(i);               // ← 名前未指定/不一致なら A,B,C,... を付ける
            } else {
                name = names.get(i).trim();
            }
            int newId = this.idSeq.getAndIncrement();        // ← ID は新規払い出し（連番が進む）
            Member m = new Member(newId, name, MemberType.PERSON);
            m.setStatus(MemberStatus.ACTIVE);
            this.members.put(newId, m);
            this.idToIndex.put(newId, i);
            this.indexToId.add(newId);
        }
    }

    /** 0→A, 1→B, ... 25→Z, 26→AA のように Excel 風の名前を作る簡易関数 */
    private static String autoName(int index){
        StringBuilder sb = new StringBuilder();
        int x = index;
        do{
            int d = x % 26;
            sb.insert(0, (char)('A' + d));
            x = (x / 26) - 1;
        }while(x >= 0);
        return sb.toString();
    }
}

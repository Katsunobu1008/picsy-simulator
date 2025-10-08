package com.example.picsy_engine.service;

import com.example.picsy_engine.api.dto.*;
import com.example.picsy_engine.domain.Member;
import com.example.picsy_engine.domain.MemberType;
import com.example.picsy_engine.state.StateStore;
import com.example.picsy_engine.util.MatrixUtils;
import com.example.picsy_engine.ContributionCalculator;
import org.ejml.simple.SimpleMatrix;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 事業ロジックを一手に引き受ける層。
 * - 現在状態の計算
 * - 行列置換（正規化）
 * - 自然回収
 * - 定価取引（δ=α*c_b）
 * - メンバー追加（既存c不変レシピ）
 * - ゴースト化（死）
 * - 会社設立（投資・予算・配分）
 * - 仮想解体（人だけの行列 ˆE）
 */
@Service
public class SimulationService {

    private final StateStore store;
    private final ContributionCalculator calculator = new ContributionCalculator();
    private final ActionLogService logs;

    public SimulationService(StateStore store, ActionLogService logs){
        this.store=store; this.logs=logs;
    }

    /** 現在状態を返す（matrix, c, purchasingPower） */
    public StateResponse getState(){
        var members = store.listMembers();
        var matrix = store.matrixCopy();

        // c を反復法で算出
        var cMat = calculator.calculate(new SimpleMatrix(matrix));
        double[] contributions = new double[members.size()];
        for(int i=0;i<contributions.length;i++) contributions[i]=cMat.get(i,0);

        // 購買力 = E_ii * c_i
        double[] power = new double[members.size()];
        for(int i=0;i<power.length;i++) power[i]= matrix[i][i]*contributions[i];

        return new StateResponse(toViews(members), matrix, contributions, power);
    }

    /** 行列の置換（行正規化して保存） */
    public StateResponse updateMatrix(UpdateMatrixRequest req){
        double[][] m = MatrixUtils.copy(req.matrix());
        MatrixUtils.normalizeRowsInPlace(m);
        store.replaceMatrix(m);
        logs.log("MATRIX","matrix replaced & normalized");
        return getState();
    }

    /** 自然回収（ゴースト行は対象外） */
    public StateResponse recover(double gamma){
        double[][] E = store.matrixCopy();
        int n = E.length;
        for(int i=0;i<n;i++){
            if(store.isGhostRow(i)) continue; // ゴーストは停止
            double eii = E[i][i];
            for(int j=0;j<n;j++) if(j!=i) E[i][j] *= (1.0 - gamma);
            E[i][i] = eii + gamma*(1.0 - eii);
        }
        MatrixUtils.normalizeRowsInPlace(E);
        store.replaceMatrix(E);
        logs.log("RECOVERY","gamma="+gamma);
        return getState();
    }

    /** 定価取引：α = δ / c_b、E_bb-=α、E_bs+=α */
    public StateResponse transact(TransactionRequest req){
        int b = indexOrThrow(req.buyerId());
        int s = indexOrThrow(req.sellerId());
        if(b==s) throw new IllegalArgumentException("buyer==seller");

        double[][] E = store.matrixCopy();
        double c_b = calculator.calculate(new SimpleMatrix(E)).get(b,0);
        if(c_b<=0) throw new IllegalArgumentException("buyer contribution is zero");
        double alpha = req.price() / c_b;

        if(alpha < 0) throw new IllegalArgumentException("alpha < 0");
        if(alpha > E[b][b]+1e-12) throw new IllegalArgumentException("insufficient budget");

        E[b][b]-=alpha;
        E[b][s]+=alpha;

        MatrixUtils.normalizeRowsInPlace(E);
        store.replaceMatrix(E);
        logs.log("TRANSACTION", "buyer="+req.buyerId()+", seller="+req.sellerId()+", delta="+req.price()+", alpha="+alpha);
        return getState();
    }

    /** メンバー追加（VCBに基づく既存c不変レシピ） */
    public StateResponse addMember(AddMemberRequest req){
        String name = req.name().trim();
        if(name.isEmpty()) throw new IllegalArgumentException("name empty");

        int n = store.size();
        double[][] E = store.matrixCopy();
        var cMat = calculator.calculate(new SimpleMatrix(E));
        double[] c = new double[n];
        for(int i=0;i<n;i++) c[i]=cMat.get(i,0);

        double[][] Ex = new double[n+1][n+1];

        // 既存行のオフ対角は (N-1)/N 倍、新列は (1-E_ii)/N、対角はそのまま
        for(int i=0;i<n;i++){
            double eii = E[i][i];
            for(int j=0;j<n;j++){
                if(j==i) Ex[i][i]=eii;
                else Ex[i][j]=E[i][j]*((double)(n-1)/n);
            }
            Ex[i][n]=(1.0 - eii)/n;
        }
        // 新行：対角0、既存への配分は c_i/N
        for(int j=0;j<n;j++) Ex[n][j]=c[j]/n;
        Ex[n][n]=0.0;

        MatrixUtils.normalizeRowsInPlace(Ex);

        int newId = store.allocateId(name,false);
        store.attachNewIndexForId(newId, Ex);
        logs.log("MEMBER_ADD","id="+newId+" name="+name);
        return getState();
    }

    /** ゴースト化（死）→ 自然回収から除外（徐々に影響が消える） */
    public StateResponse ghost(int memberId){
        store.markGhost(memberId);
        logs.log("GHOST", "id="+memberId);
        return getState();
    }

    /** 会社設立（投資・予算・配分） */
    public StateResponse createCompany(CompanyCreateRequest req){
        int n=store.size();
        double[][] E = store.matrixCopy();

        int companyId = store.allocateId(req.name(), true);

        double[][] Ex = new double[n+1][n+1];
        for(int i=0;i<n;i++) System.arraycopy(E[i],0,Ex[i],0,n);

        // founders: invest を X 列に立て、既存オフ対角を比例縮小
        for(var f: req.founders()){
            int i = indexOrThrow(f.memberId());
            double invest = f.invest();
            if(invest<0) throw new IllegalArgumentException("negative invest");
            double residual = 1.0 - Ex[i][i];
            if(invest>residual+1e-12) throw new IllegalArgumentException("invest > residual");

            double scale = (residual - invest)/residual;
            for(int j=0;j<n;j++) if(j!=i) Ex[i][j]*=scale;
            Ex[i][n]=invest;
        }

        // X 行：E_XX=budget、残り(1-budget)を weight 比で人へ
        Ex[n][n]=req.budget();
        double wsum=0.0;
        for(var o: req.outflows()) wsum += o.weight();
        if(wsum<=0) throw new IllegalArgumentException("outflow sum <= 0");
        for(var o: req.outflows()){
            int j = indexOrThrow(o.memberId());
            Ex[n][j]=(1.0 - req.budget())*(o.weight()/wsum);
        }

        MatrixUtils.normalizeRowsInPlace(Ex);
        store.attachNewIndexForId(companyId, Ex);
        logs.log("COMPANY_CREATE","id="+companyId+" name="+req.name()+" budget="+req.budget());
        return getState();
    }

    /** 仮想解体：人だけの行列 ˆE を返す（UIの解体ビュー用） */
    public DecomposeResponse decomposeCompany(int companyId){
        int x = indexOrThrow(companyId);
        double[][] E = store.matrixCopy();
        var all = store.listMembers();

        List<Integer> peopleIdx = new ArrayList<>();
        List<Member> people = new ArrayList<>();
        for(int i=0;i<all.size();i++){
            if(all.get(i).getType()== MemberType.PERSON){ peopleIdx.add(i); people.add(all.get(i)); }
        }
        int m = peopleIdx.size();
        double[][] H = new double[m][m];

        double denom = 1.0 - E[x][x];
        if(denom<=1e-12) denom=1e-12;

        for(int a=0;a<m;a++){
            int i=peopleIdx.get(a);
            for(int b=0;b<m;b++){
                int j=peopleIdx.get(b);
                H[a][b] = E[i][j] + E[i][x]*(E[x][j]/denom);
            }
        }
        MatrixUtils.normalizeRowsInPlace(H);

        return new DecomposeResponse(toViews(people), H);
    }

    // ===== helpers =====

    private int indexOrThrow(int memberId){
        Optional<Integer> oi = store.indexOfId(memberId);
        if(oi.isEmpty()) throw new IllegalArgumentException("unknown memberId="+memberId);
        return oi.get();
    }

    private List<MemberView> toViews(List<Member> list){
        List<MemberView> out=new ArrayList<>();
        for(Member m:list){
            out.add(new MemberView(
                m.getId(), m.getName(), m.getType().name(), m.getStatus().name()
            ));
        }
        return out;
    }
}

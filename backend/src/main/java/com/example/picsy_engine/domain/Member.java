package com.example.picsy_engine.domain;

/**
 * メンバーの属性（ID/名前/種別/状態）を表すドメインオブジェクト。
 * 行列 E の index とは独立した「永続ID」を持つ（UIの安定性のため）。
 */
public class Member {
    private final int id;
    private String name;
    private MemberType type;
    private MemberStatus status;

    public Member(int id, String name, MemberType type){
        this.id = id; this.name = name; this.type = type;
        this.status = MemberStatus.ACTIVE;
    }
    public int getId(){return id;}
    public String getName(){return name;}
    public MemberType getType(){return type;}
    public MemberStatus getStatus(){return status;}

    public void setName(String name){this.name=name;}
    public void setType(MemberType type){this.type=type;}
    public void setStatus(MemberStatus status){this.status=status;}
}
